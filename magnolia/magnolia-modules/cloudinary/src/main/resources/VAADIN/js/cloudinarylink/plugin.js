(function() {
    var EVENT_SEND_MAGNOLIA_LINK = "mgnlLinkSelected";
    var EVENT_SEND_CLOUDINARY_LINK = "cloudinaryLinkSelected";
    var EVENT_CANCEL_LINK = "mgnlLinkCancel";
    var EVENT_GET_MAGNOLIA_LINK = "mgnlGetLink";
    var COMMAND_CLOUDINARY_LINK = 'cloudinaryLink';
    var EVENT_GET_CLOUDINARY_LINK = 'getCloudinaryLink';
    var WORKSPACE_WEBSITE = "website";
    var WORKSPACES = [WORKSPACE_WEBSITE];
    var selectedCommand = null;
    var VAADIN_DIR_URL = typeof CKEDITOR.vaadinDirUrl !== 'undefined'? CKEDITOR.vaadinDirUrl : "../../../";

    CKEDITOR.plugins.add('magnolialink', {
        init: function(editor) {
            editor.ui.addButton('InternalLink', {
                label: 'Link to Magnolia Page',
                command: WORKSPACE_WEBSITE,
                icon: VAADIN_DIR_URL + "themes/resurface/img/link-page.svg"
            });

            editor.ui.addButton('DamLink', {
                label: 'Link to Cloudinary Asset',
                command: COMMAND_CLOUDINARY_LINK,
                icon: VAADIN_DIR_URL + "themes/resurface/img/link-assets.svg"
            });

            editor.addMenuGroup('mlinkgroup');

            editor.addMenuItem('website', {
                label: 'Edit Magnolia Link',
                command: WORKSPACE_WEBSITE,
                group: 'mlinkgroup',
                icon: VAADIN_DIR_URL + "themes/resurface/img/link-page.svg"
            });

            editor.addMenuItem('dam', {
                label: 'Edit Cloudinary Link',
                command: COMMAND_CLOUDINARY_LINK,
                group: 'mlinkgroup',
                icon: VAADIN_DIR_URL + "themes/resurface/img/link-assets.svg"
            });

            /*
             * Firefox will lose focus when popping up a dialog. So a variable
             * is needed to restore selection after dialog has closed.
             */
            var selectionRangeHack = null;

            function reqDialog(workspace) {
                return function(editor) {
                    selectCommand(editor, workspace);
                    selectionRangeHack = editor.getSelection().getRanges(true);
                    var selectedElement = CKEDITOR.plugins.link.getSelectedLink(editor);

                    if (isInternalLink(selectedElement)) {
                        var href = selectedElement.getAttribute('href');
                        var path = href.match(/path\:\{([^\}]*)\}/);
                        var repository = getWorkspace(selectedElement);

                        if(!path) {
                            path = href.match(/handle\:\{([^\}]*)\}/);
                        }

                        console.log('*** Fire -> mgnlGetLink ***');
                        editor.fire(EVENT_GET_MAGNOLIA_LINK, '{\'workspace\' :\''+ repository+'\', \'path\': \''+path[1]+'\'}');
                    } else {
                        console.log('*** Fire -> mgnlGetLink ***');
                        editor.fire(EVENT_GET_MAGNOLIA_LINK, '{\'workspace\' :\''+ workspace+'\'}');
                    }

                    setReadOnly(editor, true);
                };
            }

            function reqCloudinaryDialog() {
                return function(editor) {
                    selectCommand(editor, COMMAND_CLOUDINARY_LINK);
                    selectionRangeHack = editor.getSelection().getRanges(true);

                    console.log('*** Fire -> getCloudinaryLink ***');
                    editor.fire(EVENT_GET_CLOUDINARY_LINK, null);

                    setReadOnly(editor, true);
                };
            }

            // Setup commands requesting App dialogs
            for (var index = 0; index < WORKSPACES.length; index++) {
                editor.addCommand(WORKSPACES[index], {
                    exec: reqDialog(WORKSPACES[index])
                });
            }

            editor.addCommand('cloudinaryLink', {
                exec: reqCloudinaryDialog()
            });

            // Respond from Pages app
            editor.on(EVENT_SEND_MAGNOLIA_LINK, function(e) {
                deselectedCommand();
                setReadOnly(editor, false);
                editor.getSelection().selectRanges(selectionRangeHack);
                var selectedElement = CKEDITOR.plugins.link.getSelectedLink(editor);
                var link = eval('('+e.data+')');
                var href = '${link:{uuid:{'+link.identifier+
                    '},repository:{'+link.repository+
                    '},path:{'+link.path+
                    '}}}';

                if (isLink(selectedElement)) {
                    selectedElement.setAttribute('href', href);
                    selectedElement.setAttribute('data-cke-saved-href', href);
                    editor.focus();
                } else {
                    var selectedText = editor.getSelection();
                    var elem = editor.document.createElement('a');
                    elem.setAttribute('href', href);

                    if (selectedText && selectedText.getSelectedText() !== '') {
                        elem.setHtml(selectedText.getSelectedText());
                    } else {
                        elem.setHtml(link.caption);
                    }

                    editor.insertElement(elem);
                }
            });

            editor.on(EVENT_SEND_CLOUDINARY_LINK, function(e) {
                deselectedCommand();
                setReadOnly(editor, false);

                var cloudinaryLink = JSON.parse(e.data);

                if (!(Object.keys(cloudinaryLink).length === 0 && cloudinaryLink.constructor === Object)) {
                    var selectedText = editor.getSelection();
                    var elem = editor.document.createElement('a');

                    elem.setAttribute('href', cloudinaryLink.url);

                    if (selectedText && selectedText.getSelectedText() != '') {
                        elem.setHtml(selectedText.getSelectedText());

                    } else {
                        elem.setHtml(cloudinaryLink.name);
                    }

                    editor.insertElement(elem);
                }
            });

            editor.on(EVENT_CANCEL_LINK, function(e) {
                deselectedCommand();
                if (e.data) {
                    alert(e.data);
                }

                setReadOnly(editor, false);
            });

            // Double click
            editor.on('doubleclick', function(ev) {
                var selected = CKEDITOR.plugins.link.getSelectedLink(editor);

                if (isInternalLink(selected)) {
                    ev.data.dialog = null; // Eat the CK link dialog
                    var workspace = getWorkspace(selected);
                    editor.getCommand(workspace).exec(editor);
                    selectCommand(editor, workspace);
                }
            });

            // Selection change
            editor.on( 'selectionChange', function( evt ) {
                if ( editor.readOnly ) {
                    return;
                }

                var element = evt.data.path.lastElement && evt.data.path.lastElement.getAscendant( 'a', true );

                if (isLink(element)) {
                    for (var index = 0; index < WORKSPACES.length; index++) {
                        editor.getCommand(WORKSPACES[index]).setState(CKEDITOR.TRISTATE_DISABLED);
                    }
                    editor.getCommand('unlink').setState(CKEDITOR.TRISTATE_OFF);

                    var workspace = getWorkspace(element);
                    if (workspace) {
                        editor.getCommand(workspace).setState(CKEDITOR.TRISTATE_OFF);
                    }
                } else {
                    for (var index = 0; index < WORKSPACES.length; index++) {
                        editor.getCommand(WORKSPACES[index]).setState(CKEDITOR.TRISTATE_OFF);
                    }

                    editor.getCommand('link').setState(CKEDITOR.TRISTATE_OFF);
                    editor.getCommand('unlink').setState(CKEDITOR.TRISTATE_DISABLED);
                }
            });

            // Context menu
            editor.contextMenu.addListener(function(element, selection) {
                if (!isInternalLink(element)) {
                    return null;
                }

                var retval = {};
                retval[getWorkspace(element)] = CKEDITOR.TRISTATE_OFF;
                return retval;
            });
        }
    });

    function getWorkspace(element) {
        var href = element.getAttribute('href');
        var repository = href.match(/repository\:\{([^\}]*)\}/);

        if (repository) {
            return repository[1];
        } else {
            return false;
        }
    }

    function setReadOnly(editor, isReadOnly) {
        if (isReadOnly) {
            editor.element.setStyle('opacity', '0.2');
        } else {
            editor.element.removeStyle('opacity');
        }
    }

    function isInternalLink(element) {
        if (isLink(element) && element.getAttribute('href').substring(0,1) == '$') {
            return true;
        }

        return false;
    };

    function isLink(element) {
        if (element && element.getName().toLowerCase() == 'a' && element.getAttribute('href') && element.getChildCount()) {
            return true;
        } else {
            return false;
        }
    }

    function selectCommand(editor, commandName) {
        selectedCommand = editor.getCommand(commandName);
        if (selectedCommand != null) {
            selectedCommand.disable();
        }
    }

    function deselectedCommand() {
        if (selectedCommand != null) {
            selectedCommand.enable();
            selectedCommand = null;
        }
    }
})();
