(function () {
    let EVENT_VIDEO_CHOSEN = "videoChosen",
        EVENT_VTT_CHOSEN = "chapterChosen",
        EVENT_CANCEL_LINK = "mgnlLinkCancel",

        COMMAND_CLOUDINARY_VIDEO = 'cloudinaryVideo';

    CKEDITOR.plugins.add('cloudinaryVideoPlayer', {
        lang: ['en', 'pl'],
        init: function (editor) {
            editor.ui.addButton('Cloudinaryplayer', {
                label: 'Cloudinary Video',
                command: COMMAND_CLOUDINARY_VIDEO,
                icon: this.path + 'images/icon.png'
            });

            editor.addCommand(COMMAND_CLOUDINARY_VIDEO,
              new CKEDITOR.dialogCommand( 'cloudinaryVideoDialog', {
                  allowedContent: 'div{*}(*); iframe{*}[!width,!height,!src,!frameborder,!allowfullscreen,!allow]; object param[*]; a[*]; img[*]'
              } ) );
            CKEDITOR.dialog.add( 'cloudinaryVideoDialog', this.path + 'dialogs/video.js' );

            // Updates the target element with the id of uploaded/selected file.
            //
            // @param {String}
            //            publicId The id of a file.
            function updateTargetElement( publicId, sourceElement ) {
                var dialog = sourceElement.getDialog();
                var targetElement = sourceElement.filebrowser.target || null;

                // If there is a reference to targetElement, update it.
                if ( targetElement ) {
                    var target = targetElement.split( ':' );
                    var element = dialog.getContentElement( target[ 0 ], target[ 1 ] );
                    if ( element ) {
                        element.setValue( publicId );
                        dialog.selectPage( target[ 0 ] );
                    }
                }
            }

            function setFileBrowserValue( value, fileBrowserSe) {
                if ( value ) {
                    updateTargetElement( value, fileBrowserSe);
                }
            }

            editor.on(EVENT_VIDEO_CHOSEN, function (e) {

                const cloudinaryLink = JSON.parse(e.data);
                if (cloudinaryLink.cloudPlayer) {

                    const cloudinaryURL = new URL(cloudinaryLink.cloudPlayer);
                    if (cloudinaryURL !== undefined) {

                        const publicId = cloudinaryLink.publicId;
                        if (publicId !== undefined) {

                            const url = cloudinaryURL + '?public_id=' + publicId;
                            const filebrowser = this._.filebrowserSe;
                            const dialog = filebrowser.getDialog()
                            const iframe = dialog.originalElement;
                            if (iframe) {
                                iframe.setAttribute('src',url);
                                if (cloudinaryLink.width !== undefined && cloudinaryLink.height !== undefined) {
                                    iframe.setAttribute('width', cloudinaryLink.width);
                                    iframe.setAttribute('height', cloudinaryLink.height);
                                    iframe.setCustomData('isReady', 'true');
                                }
                            }

                            setFileBrowserValue(url, filebrowser);
                            dialog.setupContent( iframe );
                        }
                        else {
                            console.error("Unable to get video properties.")
                        }
                    }
                } else {
                    alert(cloudinaryLink.errorMessage || 'Selected asset is not a video.');
                }
            });

            editor.on(EVENT_VTT_CHOSEN, function (e) {

                const cloudinaryAsset = JSON.parse(e.data);
                if (cloudinaryAsset.publicId) {
                    setFileBrowserValue(cloudinaryAsset.publicId, this._.filebrowserSe);
                    if (cloudinaryAsset.url) {
                        const dialog = this._.filebrowserSe.getDialog();
                        const preview = dialog.preview;
                        if (preview !== undefined)
                            preview.setAttribute('src', cloudinaryAsset.url);
                    }
                } else {
                    alert(cloudinaryAsset.errorMessage || 'Selected asset is not a chapter file.');
                }
            });

            editor.on(EVENT_CANCEL_LINK, function (e) {
                if (e.data) {
                    alert(e.data);
                }
            });
        }
    });
})();
