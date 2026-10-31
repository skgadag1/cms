/**
 * Video dialog definition based on image definition authored by Frederico Knabben.
 */

(function () {
  var customVideoDialog = function (editor) {
    var regexGetSize = /^\s*(\d+)((px)|\%)?\s*$/i,
      regexGetSizeOrEmpty = /(^\s*(\d+)((px)|\%)?\s*$)|^$/i,
      regexNumberOrEmpty = /(^\s*(\d+)\s*$)|^$/i;

    var onSizeChange = function () {
      var value = this.getValue(),
        // This = input element.
        dialog = this.getDialog(),
        aMatch = value.match(regexGetSize); // Check value
      if (aMatch) {
        if (aMatch[2] == '%') // % is allowed - > unlock ratio.
          switchLockRatio(dialog, false); // Unlock.
        value = aMatch[1];
      }

      // Only if ratio is locked
      if (dialog.lockRatio) {
        var oImageOriginal = dialog.originalElement;
        if (oImageOriginal.getCustomData('isReady') == 'true') {
          if (this.id == 'txtHeight') {
            if (value && value != '0')
              value = Math.round(oImageOriginal.$.width * (value / oImageOriginal.$.height));
            if (!isNaN(value))
              dialog.setValueOf('info', 'txtWidth', value);
          }
          // this.id = txtWidth.
          else {
            if (value && value != '0')
              value = Math.round(oImageOriginal.$.height * (value / oImageOriginal.$.width));
            if (!isNaN(value))
              dialog.setValueOf('info', 'txtHeight', value);
          }
        }
      }
    };

    var switchLockRatio = function (dialog, value) {
      if (!dialog.getContentElement('info', 'ratioLock'))
        return null;

      var oImageOriginal = dialog.originalElement;

      // Dialog may already closed. (https://dev.ckeditor.com/ticket/5505)
      if (!oImageOriginal)
        return null;

      // Check image ratio and original image ratio, but respecting user's preference.
      if (value == 'check') {
        if (!dialog.userlockRatio && oImageOriginal.getCustomData('isReady') == 'true') {
          var width = dialog.getValueOf('info', 'txtWidth'),
            height = dialog.getValueOf('info', 'txtHeight'),
            originalRatio = oImageOriginal.$.width / oImageOriginal.$.height,
            thisRatio = width / height;

          dialog.lockRatio = false; // Default: unlock ratio

          if (!width && !height) {
            dialog.lockRatio = true;
          } else {
            // Round ratio to two decimal places so ratio locking will be less precise (#2254).
            var ratioComparison = Math.round((originalRatio / thisRatio) * 100) / 100;

            if (ratioComparison == 1) {
              dialog.lockRatio = true;
            }
          }
        }
      } else if (value !== undefined) {
        dialog.lockRatio = value;
      } else {
        dialog.userlockRatio = 1;
        dialog.lockRatio = !dialog.lockRatio;
      }

      var ratioButton = CKEDITOR.document.getById(btnLockSizesId);
      if (dialog.lockRatio)
        ratioButton.removeClass('cke_btn_unlocked');
      else
        ratioButton.addClass('cke_btn_unlocked');

      ratioButton.setAttribute('aria-checked', dialog.lockRatio);

      // Ratio button hc presentation - WHITE SQUARE / BLACK SQUARE
      if (CKEDITOR.env.hc) {
        var icon = ratioButton.getChild(0);
        icon.setHtml(dialog.lockRatio ? CKEDITOR.env.ie ? '\u25A0' : '\u25A3' : CKEDITOR.env.ie ? '\u25A1' : '\u25A2');
      }

      return dialog.lockRatio;
    };

    var resetSize = function (dialog, emptyValues) {
      var oImageOriginal = dialog.originalElement,
        ready = oImageOriginal.getCustomData('isReady') == 'true';

      if (ready) {
        var widthField = dialog.getContentElement('info', 'txtWidth'),
          heightField = dialog.getContentElement('info', 'txtHeight'),
          widthValue, heightValue;

        if (emptyValues) {
          widthValue = 0;
          heightValue = 0;
        } else {
          widthValue = oImageOriginal.$.width;
          heightValue = oImageOriginal.$.height;
        }

        widthField && widthField.setValue(widthValue);
        heightField && heightField.setValue(heightValue);
      }
    };

    var setupDimension = function (element) {
      function checkDimension(size, defaultValue) {
        var aMatch = size.match(regexGetSize);
        if (aMatch) {
          // % is allowed.
          if (aMatch[2] == '%') {
            aMatch[1] += '%';
            switchLockRatio(dialog, false); // Unlock ratio
          }
          return aMatch[1];
        }
        return defaultValue;
      }

      var dialog = this.getDialog(),
        value = '',
        dimension = this.id == 'txtWidth' ? 'width' : 'height',
        size = element.getAttribute(dimension);

      if (size)
        value = checkDimension(size, value);
      value = checkDimension(element.getStyle(dimension), value);

      this.setValue(value);
    };

    var numbering = function (id) {
        return CKEDITOR.tools.getNextId() + '_' + id;
      },
      btnLockSizesId = numbering('btnLockSizes'),
      btnResetSizeId = numbering('btnResetSize'),
      previewId = numbering( 'previewImage' );

    return {
      title: editor.lang.cloudinaryVideoPlayer.txtLabel,
      minWidth: 360,
      minHeight: 300,
      onShow: function () {
        if ( !this.originalElement ) {
          var element = editor.document.createElement('iframe');
          element.setCustomData( 'isReady', 'false' );
          this.originalElement = element;
          this.insertMode = true;
          this.lockRatio = true;
          this.userlockRatio = 0;
          this.dontResetSize = false;
          this.firstLoad = true;
        } else
          this.insertMode = false;

        this.preview = CKEDITOR.document.getById( previewId );
        if ( !this.insertMode ) {
          this.setupContent( this.originalElement );
          if ( !this.dontResetSize ) {
            resetSize( this, false);
          }

          if ( this.firstLoad ) {
            CKEDITOR.tools.setTimeout( function() {
              switchLockRatio( this, 'check' );
            }, 0, this );
          }
          this.firstLoad = false;
          this.dontResetSize = false;
        }

        switchLockRatio(this, true);
      },
      onHide: function() {
        if ( this.originalElement ) {
          this.originalElement.remove();
          this.originalElement = false; // Dialog is closed.
        }
        if (this.preview)
          this.preview.setAttribute('src', '');
      },
      onOk: function () {
        const dialog = this,
          iframe = dialog.originalElement;
        dialog.commitContent(iframe);

        let srcBase = new URL(iframe.getAttribute('src'));
        if (srcBase !== undefined) {
          const trackId = iframe.getCustomData('trackPublicId');
          if (trackId)
            srcBase.searchParams.append('track_public_id', trackId);
          const startFrom = iframe.getCustomData('txtStartFrom');
          if (startFrom)
            srcBase.searchParams.append(iframe.getCustomData('startFrom'), startFrom);

          const chkResponsive = iframe.getCustomData('chkResponsive');

          const wrapper = editor.document.createElement( 'div' );
          wrapper.setAttribute('class','iframe-wrapper');
          const cloudinaryPlayer = wrapper.append( 'iframe' );
          cloudinaryPlayer.setAttribute('src', srcBase);
          cloudinaryPlayer.setAttribute('allow', 'fullscreen; encrypted-media; picture-in-picture');
          cloudinaryPlayer.setAttribute('allowFullScreen', 'true');
          cloudinaryPlayer.setAttribute('frameBorder', '0');
          cloudinaryPlayer.setAttribute('height', iframe.getAttribute('height'));
          cloudinaryPlayer.setAttribute('width', iframe.getAttribute('width'));

          if(chkResponsive) {
            cloudinaryPlayer.setAttribute('style', 'position:absolute;top:0;left:0;width:100%;height:100%');
            wrapper.setAttribute('style', 'position:relative;padding-bottom:56.25%;padding-top:30px;height:0;overflow:hidden');
          }

          if (iframe.getAttribute('class'))
            cloudinaryPlayer.addClass(iframe.getAttribute('class'));
          editor.insertElement( wrapper );

        } else {
          console.error("Unable to get video properties.");
        }
      },
      contents: [{
        id: 'info',
        label: editor.lang.cloudinaryVideoPlayer.txtLabel,
        accessKey: 'I',
        elements: [{
          type: 'vbox',
          padding: 0,
          children: [{
            type: 'hbox',
            widths: ['290px', '110px'],
            align: 'right',
            className: 'cke_dialog_image_url',
            children: [{
              id: 'txtVideoUrl',
              type: 'text',
              label: editor.lang.cloudinaryVideoPlayer.txtVideoUrl,
              required: true,
              onChange: function () {
                var dialog = this.getDialog(),
                  newUrlId = this.getValue();

                // Update original image.
                // Prevent from load before onShow.
                if (newUrlId.length > 0) {
                  dialog = this.getDialog();
                  var original = dialog.originalElement;
                  original.setAttribute('src', newUrlId);
                }
              },
              setup: function (element) {
                  this.getDialog().dontResetSize = true;
                  this.setValue(element.getAttribute('src')); // And call this.onChange()
              },
              commit: function (element) {
                element.setAttribute('src', this.getValue());
              },
              validate: CKEDITOR.dialog.validate.notEmpty(editor.lang.image.urlMissing)
            },
              {
                type: 'button',
                id: 'browseVideo',
                className: 'cke_dialog_image_browse',
                filebrowser: {
                  action: 'Browse',
                  target: 'info:txtVideoUrl',
                  url: editor.config.filebrowserImageBrowseLinkUrl
                },
                style: 'float:right',
                hidden: true,
                label: editor.lang.common.browseServer,
              }]
          },
            {
            type: 'hbox',
            widths: ['280px', '110px'],
            align: 'right',
            className: 'cke_dialog_image_url',
            children: [{
              id: 'txtChaptersUrl',
              type: 'text',
              label: editor.lang.cloudinaryVideoPlayer.txtChapters,
              required: false,
              onLoad : function() {
                this.getInputElement().setAttribute( 'readOnly', true );
              },
              setup: function (element) {
                this.getDialog().dontResetSize = true;
                this.setValue(element.getCustomData('trackPublicId')); // And call this.onChange()
              },
              commit: function (element) {
                element.setCustomData('trackPublicId', this.getValue());
              }
            },
              {
                type: 'button',
                id: 'browseChapters',
                className: 'cke_dialog_image_browse',
                filebrowser: {
                  action: 'Browse',
                  target: 'info:txtChaptersUrl',
                  url: editor.config.filebrowserImageBrowseLinkUrl
                },
                style: 'float:right',
                hidden: true,
                label: editor.lang.common.browseServer
              }]
          },
            {
            type: 'hbox',
            widths: ['280px', '110px'],
            align: 'right',
            className: 'cke_dialog_image_browse',
            children: [{
              id : 'chkResponsive',
              type : 'checkbox',
              label : editor.lang.cloudinaryVideoPlayer.txtResponsive,
              'default' : editor.config.video_responsive != null ? editor.config.video_responsive : false,
              setup: function (element) {
                if (element.getCustomData('chkResponsive'))
                  this.setValue(element.getCustomData('chkResponsive'));
              },
              commit: function (element) {
                console.log("check value set : "+this.getValue());
                element.setCustomData('chkResponsive', this.getValue());
              }
            }]
          }, {
            type: 'hbox',
            widths: ['50%', '50%'],
            children: [{
              id: 'basic',
              type: 'hbox',
              widths: ['70%', '30%'],
              children: [{
                type: 'vbox',
                padding: 1,
                children: [{
                  type: 'text',
                  width: '100px',
                  id: 'txtWidth',
                  label: editor.lang.common.width,
                  'default' : editor.config.video_width != null ? editor.config.video_width : '640',
                  onKeyUp: onSizeChange,
                  validate: function () {
                    var aMatch = this.getValue().match(regexGetSizeOrEmpty),
                      isValid = !!(aMatch && parseInt(aMatch[1], 10) !== 0);
                    if (!isValid)
                      alert(editor.lang.common.invalidLength.replace('%1', editor.lang.common.width).replace('%2', 'px, %')); // jshint ignore:line
                    return isValid;
                  },
                  setup: setupDimension,
                  commit: function (element) {
                    var value = this.getValue();
                    if (value)
                      element.setAttribute('width', CKEDITOR.tools.cssLength(value));
                    else
                      element.removeAttribute('width');
                  }
                },
                  {
                    type: 'text',
                    id: 'txtHeight',
                    width: '100px',
                    label: editor.lang.common.height,
                    'default' : editor.config.video_height != null ? editor.config.video_height : '360',
                    onKeyUp: onSizeChange,
                    validate: function () {
                      var aMatch = this.getValue().match(regexGetSizeOrEmpty),
                        isValid = !!(aMatch && parseInt(aMatch[1], 10) !== 0);
                      if (!isValid)
                        alert(editor.lang.common.invalidLength.replace('%1', editor.lang.common.height).replace('%2', 'px, %')); // jshint ignore:line
                      return isValid;
                    },
                    setup: setupDimension,
                    commit: function (element) {
                      var value = this.getValue();
                      if (value)
                        element.setAttribute('height', CKEDITOR.tools.cssLength(value));
                      else
                        element.removeAttribute('height');
                    }
                  }]
              },
                {
                  id: 'ratioLock',
                  type: 'html',
                  style: 'margin-top:50px;width:40px;height:40px;',
                  onLoad: function () {
                    // Activate Reset button
                    var resetButton = CKEDITOR.document.getById(btnResetSizeId),
                      ratioButton = CKEDITOR.document.getById(btnLockSizesId);
                    if (resetButton) {
                      resetButton.on('click', function (evt) {
                        resetSize(this);
                        evt.data && evt.data.preventDefault();
                      }, this.getDialog());
                      resetButton.on('mouseover', function () {
                        this.addClass('cke_btn_over');
                      }, resetButton);
                      resetButton.on('mouseout', function () {
                        this.removeClass('cke_btn_over');
                      }, resetButton);
                    }
                    // Activate (Un)LockRatio button
                    if (ratioButton) {
                      ratioButton.on('click', function (evt) {
                        switchLockRatio(this);

                        var oImageOriginal = this.originalElement,
                          width = this.getValueOf('info', 'txtWidth');

                        if (oImageOriginal.getCustomData('isReady') == 'true' && width) {
                          var height = oImageOriginal.$.height / oImageOriginal.$.width * width;
                          if (!isNaN(height)) {
                            this.setValueOf('info', 'txtHeight', Math.round(height));
                          }
                        }
                        evt.data && evt.data.preventDefault();
                      }, this.getDialog());
                      ratioButton.on('mouseover', function () {
                        this.addClass('cke_btn_over');
                      }, ratioButton);
                      ratioButton.on('mouseout', function () {
                        this.removeClass('cke_btn_over');
                      }, ratioButton);
                    }
                  },
                  html: '<div>' +
                    '<a href="javascript:void(0)" tabindex="-1" title="' + editor.lang.image.lockRatio +
                    '" class="cke_btn_locked" id="' + btnLockSizesId + '" role="checkbox"><span class="cke_icon"></span><span class="cke_label">' + editor.lang.image.lockRatio + '</span></a>' +
                    '<a href="javascript:void(0)" tabindex="-1" title="' + editor.lang.image.resetSize +
                    '" class="cke_btn_reset" id="' + btnResetSizeId + '" role="button"><span class="cke_label">' + editor.lang.image.resetSize + '</span></a>' +
                    '</div>'
                }]
            }, {
                type: 'text',
                id: 'txtStyleClass',
                width: '110px',
                label: editor.lang.cloudinaryVideoPlayer.txtCssClass,
                style: 'float:right',
                'default': '',
                setup: function (element) {
                  this.setValue(element.getAttribute('class'));
                },
                commit: function (element) {
                  var value = this.getValue();
                  if (value)
                    element.setAttribute('class', value);
                  else
                    element.removeAttribute('class');
                }
              }]
          }, {
            type: 'hbox',
            widths: ['25%', '75%'],
            align: 'right',
            children: [{
              type: 'select',
              id: 'startFrom',
              width: '100px',
              label: editor.lang.cloudinaryVideoPlayer.txtStartFrom,
              items: [[ editor.lang.cloudinaryVideoPlayer.txtTimestamp, 't'],
                [ editor.lang.cloudinaryVideoPlayer.txtChapter, 'chapter']],
              'default': 't',
              setup: function (element) {
                if (element.getCustomData('startFrom'))
                  this.setValue(element.getCustomData('startFrom'));
              },
              commit: function (element) {
                element.setCustomData('startFrom', this.getValue());
              }
            }, {
              type: 'text',
              id: 'txtStartFrom',
              width: '70px',
              'default': '',
              validate: function () {
                var aMatch = this.getValue().match(regexNumberOrEmpty),
                  isValid = !!(aMatch && parseInt(aMatch[1], 10) !== 0);
                if (!isValid)
                  alert('Start from input must be numeric!'); // jshint ignore:line
                return isValid;
              },
              setup: function (element) {
                this.setValue(element.getCustomData('txtStartFrom'));
              },
              commit: function (element) {
                element.setCustomData('txtStartFrom', this.getValue());
              }
            }]
          }]
        }, {
          type: 'html',
          id: 'htmlPreview',
          style: 'width:100%;',
          html: '<div>' + CKEDITOR.tools.htmlEncode( editor.lang.cloudinaryVideoPlayer.txtChaptersPreview ) + '<br>' +
            '<div style="border:1px ridge #bcbcbc;height:150px;width:100%;padding:2px;background-color:white;">' +
            '<iframe id="' + previewId + '" src=""' +
            ' style="height:100%;width:100%" />' +
            '</div></div>'
        }]
      }]
    };
  };

  CKEDITOR.dialog.add('cloudinaryVideoDialog', function (editor) {
    return customVideoDialog(editor);
  });
})();
