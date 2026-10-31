package com.tbscg.doosan.magnolia.concepts.service;

import javax.jcr.Node;

/**
 * This interface defines a basic API for operations on concepts workspace.
 *
 * @author Alejandro Puig Escobar
 **/

public interface ConceptsService {

    /**
     * More flexible search with support of uuid and path.
     *
     * @param pathOrId concept node path or UUID.
     * @return concept value.
     * @throws ConceptNotFoundException in case that concept value is null or any other problem
     * with repository occurs.
     */
    String getConcept(String pathOrId) throws ConceptNotFoundException;

    /**
     * Retrieves concept value from node itself.
     *
     * @param conceptNode concept node.
     * @return concept value.
     * @throws ConceptNotFoundException in case that concept value is null or any other problem
     * with repository occurs.
     */
    String getConcept(Node conceptNode) throws ConceptNotFoundException;

    /**
     * Retrieves concept value for a specific language. In case such a language doesn't exist it fallbacks to the
     * default content language.
     *
     * @param conceptNode concept node to take value from .
     * @param language language in ISO 639-1 format.
     * @return localized concept.
     */
    String getLocalizedConcept(Node conceptNode, String language);

    /**
     * Read concept by its UUID
     *
     * @param uuid concept UUID.
     * @return concept value.
     * @throws ConceptNotFoundException in case that concept value is null or any other problem
     * with repository occurs.
     */
    String getConceptByUUID(String uuid) throws ConceptNotFoundException;

    /**
     * Read concept by node path.
     *
     * @param path concept path.
     * @return concept value.
     * @throws ConceptNotFoundException in case that concept value is null or any other problem
     * with repository occurs.
     */
    String getConceptByPath(String path) throws ConceptNotFoundException;

    /**
     * Check if a concept folder is empty
     *
     * @param path concept folder path.
     * @return true if the folder is empty, false otherwise.
     */
    boolean isConceptFolderEmpty(String path);
}

