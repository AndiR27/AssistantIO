
export default {
  bootstrap: () => import('./main.server.mjs').then(m => m.default),
  inlineCriticalCss: true,
  baseHref: '/',
  locale: undefined,
  routes: [
  {
    "renderMode": 1,
    "redirectTo": "/home",
    "route": "/"
  },
  {
    "renderMode": 1,
    "route": "/home"
  },
  {
    "renderMode": 1,
    "route": "/home/courses/*"
  },
  {
    "renderMode": 1,
    "route": "/**"
  }
],
  entryPointToBrowserMapping: undefined,
  assets: {
    'index.csr.html': {size: 23713, hash: '4abbc8b8ea1f1996375765387c23d45e60d32fa01feebb5a6672c2feef6b1e3f', text: () => import('./assets-chunks/index_csr_html.mjs').then(m => m.default)},
    'index.server.html': {size: 17136, hash: 'e27c3fdafdf58df5d61648400036f271d59fddf6c54ee93ba724a67c4732e018', text: () => import('./assets-chunks/index_server_html.mjs').then(m => m.default)},
    'styles-PLAELPIT.css': {size: 9085, hash: 'ec5h4zzYFcQ', text: () => import('./assets-chunks/styles-PLAELPIT_css.mjs').then(m => m.default)}
  },
};
