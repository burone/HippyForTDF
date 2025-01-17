/* eslint-disable no-undef */
/* eslint-disable no-underscore-dangle */


function transferToUnifiedDimensions(nativeDimensions) {
  let nativeWindow;
  let nativeScreen;
  if (global.__HIPPYNATIVEGLOBAL__.OS === 'ios') {
    ({ window: nativeWindow, screen: nativeScreen  } = nativeDimensions);
  } else {
    ({ windowPhysicalPixels: nativeWindow, screenPhysicalPixels: nativeScreen } = nativeDimensions);
  }
  return {
    nativeWindow,
    nativeScreen,
  };
}

function getProcessedDimensions(nativeDimensions) {
  let window = {};
  let screen = {};
  const { nativeWindow, nativeScreen } = transferToUnifiedDimensions(nativeDimensions);
  if (nativeWindow) {
    // android is physical resolution, divided by scale needed
    global.__HIPPYNATIVEGLOBAL__.OS === 'ios'
      ? window = nativeWindow
      : window = {
        width: nativeWindow.width,
        height: nativeWindow.height,
        scale: nativeWindow.scale,
        fontScale: nativeWindow.fontScale,
        statusBarHeight: nativeWindow.statusBarHeight,
        navigatorBarHeight: nativeWindow.navigationBarHeight,
      };
  }
  if (nativeScreen) {
    // android is physical resolution, divided by scale needed
    global.__HIPPYNATIVEGLOBAL__.OS === 'ios'
      ? screen = nativeScreen
      : screen = {
        width: nativeScreen.width,
        height: nativeScreen.height,
        scale: nativeScreen.scale,
        fontScale: nativeScreen.fontScale,
        statusBarHeight: nativeScreen.statusBarHeight,
        navigatorBarHeight: nativeScreen.navigationBarHeight,
      };
  }
  return {
    window,
    screen,
  };
}

const Dimensions = {
  get(key) {
    const device = Hippy.device || {};
    return device[key];
  },
  set(nativeDimensions) {
    if (!nativeDimensions) {
      return;
    }
    const { window, screen } = getProcessedDimensions(nativeDimensions);
    Hippy.device.window = window;
    Hippy.device.screen = screen;
    Hippy.device.pixelRatio = Hippy.device.window.scale;
  },
  init() {
    this.set(__HIPPYNATIVEGLOBAL__.Dimensions);
  },
};

Dimensions.init();

__GLOBAL__.jsModuleList = {
  Dimensions,
};
