package com.tbscg.doosan.magnolia.regions.observation;

import com.google.common.collect.Streams;
import com.tbscg.doosan.magnolia.dxp.core.util.EventConstants;
import com.tbscg.doosan.magnolia.regions.util.RegionsConstants;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SimpleContext;
import info.magnolia.context.SystemContext;
import info.magnolia.module.observation.BaseConfiguration;
import info.magnolia.module.observation.ObservationConfiguration;
import info.magnolia.module.observation.commands.CommandEventListener;
import info.magnolia.objectfactory.Components;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.ocm.repository.NodeUtil;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link CommandEventListener} executed when an event occurs on regions workspace.
 *
 * @author Alejandro Puig Escobar
 */
@Slf4j
public class RegionsEventListener extends CommandEventListener implements BaseConfiguration {

    private ObservationConfiguration obsConf;

    @Override
    public ObservationConfiguration getConfiguration() {
        return this.obsConf;
    }

    @Override
    public void setConfiguration(ObservationConfiguration obsConf) {
        this.obsConf = obsConf;
    }

    @Override
    public void onEvent(EventIterator eventsIt) {
        log.debug("Checking event for command {} invocation", getCommand().getClass().getName());

        // initialize context
        Context ctx = MgnlContext.getWebContextOrNull();

        List<Event> events = this.filterEvents(eventsIt);

        for (Event event : events) {
            this.processEvent(event);
        }

        MgnlContext.setInstance(ctx);
    }

    private void processEvent(Event event) {
        try {
            Context executionCtx = new SimpleContext(Components.getComponent(SystemContext.class));
            MgnlContext.setInstance(executionCtx);

            if (this.checkIfEventIsProcessed(event)) {
                this.executeCommand(executionCtx, event);
            }

        } catch (Exception e) {
            log.error("Can't deliver event", e);
        }
    }

    private void executeCommand(Context executionCtx, Event event) {
        try {
            this.handleExecuteCommand(executionCtx, event);

        } catch (Exception e) {
            log.error("Can't execute command {}", this.getCommand().getClass().getName(), e);

        } finally {
            MgnlContext.release();
        }
    }

    private void handleExecuteCommand(Context executionCtx, Event event) throws Exception {
        if (this.getParams() != null) {
            log.debug("Command params are {}", this.getParams());
            executionCtx.putAll(this.getParams());
        }

        //Add event to context to have all information
        executionCtx.put(EventConstants.KEY, event);

        //take a look at MgnlCommand.execute(ctx) it really returns !success...
        if (!this.getCommand().execute(executionCtx)) {

            log.debug("Successfully executed command {}", this.getCommand().getClass().getName());

        } else {
            log.debug("Command {} was executed but not successfully", this.getCommand().getClass().getName());
        }
    }

    /**
     * The purpose of this method is isolating events in order to not apply the same action several times.
     *
     * @param eventIterator event iterator containing observed events.
     * @return a list of filtered events.
     */
    private List<Event> filterEvents(EventIterator eventIterator) {
        Stream<Event> eventStream = (Stream<Event>) Streams.stream(eventIterator);

        List<Event> allEvents = eventStream.collect(Collectors.toList());

        List<Event> addOrRemoveEvents = allEvents.stream().filter(event -> event.getType() == Event.NODE_ADDED || event.getType() == Event.NODE_REMOVED)
                        .collect(Collectors.toList());

        List<Event> addEvents = addOrRemoveEvents.stream().filter(event -> event.getType() == Event.NODE_ADDED).collect(Collectors.toList());

        List<Event> removeEvents = addOrRemoveEvents.stream().filter(event -> event.getType() == Event.NODE_REMOVED).collect(Collectors.toList());

        List<Event> moveEvents = allEvents.stream().filter(event -> event.getType() == Event.NODE_MOVED).collect(Collectors.toList());

        List<Event> propertyEvents = allEvents.stream()
                .filter(event -> event.getType() == Event.PROPERTY_ADDED || event.getType() == Event.PROPERTY_CHANGED
                        || event.getType() == Event.PROPERTY_REMOVED)
                .collect(Collectors.toList());

        Set<Event> eventsToExclude = new HashSet<>();

        this.excludePropertyEvents(addOrRemoveEvents, propertyEvents, eventsToExclude);

        this.excludeMoveEvents(addOrRemoveEvents, moveEvents, eventsToExclude);

        this.excludeEventsByHierarchy(addEvents, eventsToExclude);

        this.excludeEventsByHierarchy(removeEvents, eventsToExclude);

        allEvents.removeAll(eventsToExclude);

        return allEvents;
    }

    /**
     * Excludes add or remove events in case that target nodes are moved. Move event doesn't only trigger move, but
     * also add and remove.
     *
     * @param addOrRemoveEvents add or remove events to filter.
     * @param moveEvents move events to consider.
     * @param eventsToExclude set containing events to be excluded.
     */
    private void excludeMoveEvents(List<Event> addOrRemoveEvents, List<Event> moveEvents, Set<Event> eventsToExclude) {

        for (Event moveEvent : moveEvents) {

            for (Event addOrRemoveEvent : addOrRemoveEvents) {

                try {
                    if (addOrRemoveEvent.getPath().equals(moveEvent.getInfo().get(EventConstants.SRC_ABS_PATH)) ||
                            addOrRemoveEvent.getPath().equals(moveEvent.getInfo().get(EventConstants.DEST_ABS_PATH))) {
                        eventsToExclude.add(addOrRemoveEvent);
                    }
                } catch (Exception e) {/*silent exception*/}
            }
        }
    }

    /**
     * Excludes events to be processed depending on their hierarchy.
     *
     * @param eventsList events list to check.
     * @param eventsToExclude set containing events to be excluded.
     */
    private void excludeEventsByHierarchy(List<Event> eventsList, Set<Event> eventsToExclude) {

        for (Event event : eventsList) {

            for (Event toCompareEvent : eventsList) {

                try {

                    if (!event.getPath().equals(toCompareEvent.getPath()) &&
                            event.getPath().contains(toCompareEvent.getPath())) {

                        eventsToExclude.add(event);

                    }

                } catch (Exception e) {/*silent exception*/}
            }
        }

    }

    /**
     * Excludes property events if their corresponding nodes are already processed.
     *
     * @param notPropertyEvents all not property events.
     * @param propertyEvents property events.
     * @param eventsToExclude set of events to exclude.
     */
    private void excludePropertyEvents(List<Event> notPropertyEvents, List<Event> propertyEvents, Set<Event> eventsToExclude) {

        for (Event propertyEvent : propertyEvents) {

            for (Event notPropertyEvent : notPropertyEvents) {

                try {

                    String nodePropertyPath = StringUtils.substringBeforeLast(propertyEvent.getPath(), NodeUtil.PATH_SEPARATOR);

                    if (nodePropertyPath.equals(notPropertyEvent.getPath())) {
                        eventsToExclude.add(propertyEvent);
                    }

                } catch (Exception e) {/*silent exception*/}

            }
        }

    }

    /**
     * Checks if event can be processed depending on node path and node type of target node.
     *
     * @param event event to check.
     * @return whether event can be processed further or not.
     */
    @Override
    protected boolean checkIfEventIsProcessed(Event event) {
        try {
            boolean legacyCheck = super.checkIfEventIsProcessed(event);

            switch (event.getType()) {
                case Event.PROPERTY_ADDED:
                case Event.PROPERTY_CHANGED:
                case Event.PROPERTY_REMOVED:
                    return legacyCheck;
            }

            try {
                String path = event.getPath();
                Node node = MgnlContext.getJCRSession(getConfiguration().getRepository()).getNode(path);

                if (node.getDepth() == 0) {
                    return false;
                }

                String nodeType = node.getPrimaryNodeType().getName();

                return (nodeType.equals(RegionsConstants.NodeTypes.Website.NAME)
                        || nodeType.equals(RegionsConstants.NodeTypes.Region.NAME)
                        || nodeType.equals(RegionsConstants.NodeTypes.Language.NAME))
                        && legacyCheck;

            } catch (PathNotFoundException e) {
                return event.getType() == Event.NODE_REMOVED && legacyCheck;
            }

        } catch (RepositoryException e) {
            return false;
        }
    }
}
