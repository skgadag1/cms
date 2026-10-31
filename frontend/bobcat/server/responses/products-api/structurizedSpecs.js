// this has to match perfectly the structurized content needed by ProductSpecification.vue/ProductSpecItem.vue component

export default {
  models: [{id: 'Mtest'}],
  sections: {
    'section-key-capacities': {
      properties: {fueltank: {Mtest: {id: 'tankFuel', label: 'Fuel Tank', type: 'number', unit: 'l', value: '93.7'}, label: 'Fuel Tank'}},
      title: 'Capacities'
    },
    'section-key-comprehensiveproperties': {
      properties: {identifier: {Mtest: {id: 'name', label: 'Identifier', type: 'text', value: 'T550 IT4'}, label: 'Identifier'}},
      title: 'Comprehensive Properties'
    },
    'section-key-core': {properties: {identifier: {Mtest: {id: 'name', label: 'Identifier', type: 'text', value: 'T550 IT4'}, label: 'Identifier'}}, title: 'Core'},
    'section-key-dimension': {
      properties: {
        height: {Mtest: {id: 'height', label: 'Height', type: 'number', unit: 'mm', value: '1976'}, label: 'Height'},
        heighttobuckethingepin: {
          Mtest: {id: 'heightToBucketHingePin', label: 'Height to Bucket Hinge Pin', type: 'number', unit: 'mm', value: '2908'},
          label: 'Height to Bucket Hinge Pin'
        },
        heightwithoperatorcab: {
          Mtest: {id: 'heightWithOperatorCab', label: 'Height with Operator Cab', type: 'number', unit: 'mm', value: '1976'},
          label: 'Height with Operator Cab'
        },
        length: {Mtest: {id: 'length', label: 'Length', type: 'number', unit: 'mm', value: '3378'}, label: 'Length'},
        lengthoftrackonground: {
          Mtest: {id: 'lengthOfTrackOnGround', label: 'Length of track on ground', type: 'number', unit: 'mm', value: '1402'},
          label: 'Length of track on ground'
        },
        lengthwithoutattachment: {
          Mtest: {id: 'lengthWithoutAttachment', label: 'Length without Attachment', type: 'number', unit: 'mm', value: '2657'},
          label: 'Length without Attachment'
        },
        lengthwithstandardbucket: {
          Mtest: {id: 'lengthWithStandardBucket', label: 'Length with Standard Bucket', type: 'number', unit: 'mm', value: '3378'},
          label: 'Length with Standard Bucket'
        },
        'reach@maximumheight': {
          Mtest: {id: 'reachAtMaximumHeight', label: 'Reach @ Maximum Height', type: 'number', unit: 'mm', value: '490'},
          label: 'Reach @ Maximum Height'
        },
        trackwidth: {Mtest: {id: 'trackWidth', label: 'Track Width', type: 'number', unit: 'mm', value: '320'}, label: 'Track Width'},
        'trackwidth-optional': {
          Mtest: {id: 'trackWidthOptional', label: 'Track Width - Optional', type: 'number', unit: 'mm', value: '401'},
          label: 'Track Width - Optional'
        },
        turningradius: {Mtest: {id: 'turningRadius', label: 'Turning Radius', type: 'number', unit: 'mm', value: '2032'}, label: 'Turning Radius'},
        width: {Mtest: {id: 'width', label: 'Width', type: 'number', unit: 'mm', value: '1704'}, label: 'Width'},
        widthwithbucket: {Mtest: {id: 'widthWithBucket', label: 'Width (with bucket)', type: 'number', unit: 'mm', value: '1727'}, label: 'Width (with bucket)'}
      },
      title: 'Dimension'
    },
    'section-key-engine': {
      properties: {
        enginecooling: {Mtest: {id: 'engineCooling', label: 'Engine Cooling', type: 'text', value: 'Liquid'}, label: 'Engine Cooling'},
        enginefuel: {Mtest: {id: 'engineFuel', label: 'Engine Fuel', type: 'text', value: 'Diesel'}, label: 'Engine Fuel'},
        horsepower: {Mtest: {id: 'horsepower', label: 'Horsepower', type: 'number', unit: 'kW', value: '49.2'}, label: 'Horsepower'},
        turbochargedengine: {Mtest: {id: 'turbochargedEngine', label: 'Turbocharged Engine', type: 'option', value: 'yes'}, label: 'Turbocharged Engine'}
      },
      title: 'Engine'
    },
    'section-key-features': {
      properties: {
        acsswitchablecontrols: {
          Mtest: {id: 'acsSwitchableControls', label: 'ACS (Switchable Controls)', type: 'text', value: 'opt'},
          label: 'ACS (Switchable Controls)'
        },
        airconditioning: {Mtest: {id: 'airConditioning', label: 'Air Conditioning', type: 'text', value: 'opt'}, label: 'Air Conditioning'},
        auxiliaryhydraulics: {Mtest: {id: 'auxiliaryHydraulicsFeature', label: 'Auxiliary Hydraulics', type: 'text', value: 'std'}, label: 'Auxiliary Hydraulics'},
        backupalarm: {Mtest: {id: 'backupAlarm', label: 'Backup Alarm', type: 'text', value: 'std'}, label: 'Backup Alarm'},
        bicssysteminterlock: {Mtest: {id: 'bicsSystemInterlock', label: 'BICS System (Interlock)', type: 'text', value: 'std'}, label: 'BICS System (Interlock)'},
        'bob-tachattachmentsystem': {
          Mtest: {id: 'bobTachAttachmentSystem', label: 'Bob-Tach Attachment System', type: 'text', value: 'std'},
          label: 'Bob-Tach Attachment System'
        },
        cabheater: {Mtest: {id: 'cabHeater', label: 'Cab Heater', type: 'text', value: 'opt'}, label: 'Cab Heater'},
        deluxeinstrumentation: {Mtest: {id: 'deluxeInstrumentation', label: 'Deluxe Instrumentation', type: 'text', value: 'opt'}, label: 'Deluxe Instrumentation'},
        engineshutdown: {Mtest: {id: 'engineShutdown', label: 'Engine Shutdown', type: 'text', value: 'std'}, label: 'Engine Shutdown'},
        highflowoption: {Mtest: {id: 'highFlowOption', label: 'High Flow Option', type: 'text', value: 'opt'}, label: 'High Flow Option'},
        horn: {Mtest: {id: 'horn', label: 'Horn', type: 'text', value: 'std'}, label: 'Horn'},
        hydraulicbucketpositioning: {
          Mtest: {id: 'hydraulicBucketPositioning', label: 'Hydraulic Bucket Positioning', type: 'text', value: 'opt'},
          label: 'Hydraulic Bucket Positioning'
        },
        joystickcontrol: {Mtest: {id: 'joystickControl', label: 'Joystick Control', type: 'text', value: 'opt'}, label: 'Joystick Control'},
        operatinglights: {Mtest: {id: 'operatingLights', label: 'Operating Lights', type: 'text', value: 'std'}, label: 'Operating Lights'},
        'powerbob-tach': {Mtest: {id: 'powerBobTach', label: 'Power Bob-Tach', type: 'text', value: 'opt'}, label: 'Power Bob-Tach'},
        ridecontrol: {Mtest: {id: 'rideControl', label: 'Ride Control', type: 'text', value: 'opt'}, label: 'Ride Control'},
        suspensionseat: {Mtest: {id: 'suspensionSeat', label: 'Suspension Seat', type: 'text', value: 'std'}, label: 'Suspension Seat'},
        twospeedtravel: {Mtest: {id: 'twoSpeedTravel', label: 'Two Speed Travel', type: 'text', value: 'n-a'}, label: 'Two Speed Travel'}
      },
      title: 'Features'
    },
    'section-key-general': {properties: {identifier: {Mtest: {id: 'name', label: 'Identifier', type: 'text', value: 'T550 IT4'}, label: 'Identifier'}}, title: 'General'},
    'section-key-hydraulicsystem': {
      properties: {
        auxiliaryhighflow: {
          Mtest: {
            id: 'auxiliaryHighFlow',
            label: 'Auxiliary High Flow',
            type: 'number',
            unit: 'l/min',
            value: '101.1'
          },
          label: 'Auxiliary High Flow'
        },
        auxiliarystdflow: {Mtest: {id: 'auxiliaryStdFlow', label: 'Auxiliary Std Flow', type: 'number', unit: 'l/min', value: '64.7'}, label: 'Auxiliary Std Flow'},
        'systemrelief@quickcouplers': {
          Mtest: {
            id: 'systemReliefAtQuickCouplers',
            label: 'System Relief @ Quick Couplers',
            type: 'number',
            unit: 'bar',
            value: '241.32'
          },
          label: 'System Relief @ Quick Couplers'
        }
      },
      title: 'Hydraulic System'
    },
    'section-key-machine': {properties: {}, title: 'Machine'},
    'section-key-performance': {
      properties: {
        groundpressurerubber: {
          Mtest: {
            id: 'groundPressureRubber',
            label: 'Ground Pressure (Rubber)',
            type: 'number',
            unit: 'bar',
            value: '0.4'
          },
          label: 'Ground Pressure (Rubber)'
        },
        operatingcapacity50oftip: {
          Mtest: {id: 'roc50PercentOfTip', label: 'Operating Capacity (50% of Tip)', type: 'number', unit: 'kg', value: '1293'},
          label: 'Operating Capacity (50% of Tip)'
        },
        operatingweight: {Mtest: {id: 'operatingWeight', label: 'Operating Weight', type: 'number', unit: 'kg', value: '3475'}, label: 'Operating Weight'},
        ratedoperatingcapacityiso: {
          Mtest: {id: 'ratedOperatingCapacitySae', label: 'Rated Operating Capacity (ISO)', type: 'number', unit: 'kg', value: '905'},
          label: 'Rated Operating Capacity (ISO)'
        },
        tippingload: {Mtest: {id: 'tippingLoad', label: 'Tipping Load', type: 'number', unit: 'kg', value: '2585'}, label: 'Tipping Load'},
        travelspeed: {Mtest: {id: 'travelSpeed', label: 'Travel Speed', type: 'number', unit: 'km/h', value: '11.7'}, label: 'Travel Speed'},
        'travelspeed2-speedoption': {
          Mtest: {id: 'travelSpeed2SpeedOption', label: 'Travel Speed (2-speed option)', type: 'number', value: 'N/A'},
          label: 'Travel Speed (2-speed option)'
        }
      },
      title: 'Performance'
    },
    'section-key-refillcapacities': {
      properties: {fueltank: {Mtest: {id: 'capacityFuelTank', label: 'Fuel Tank', type: 'number', value: 'N/A'}, label: 'Fuel Tank'}},
      title: 'Refill Capacities'
    }
  }
}
