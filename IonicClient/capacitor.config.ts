import type { CapacitorConfig } from '@capacitor/cli';
import { mapsApiKey } from './mapsApiKey';

const config: CapacitorConfig = {
  appId: 'io.ionic.starter',
  appName: 'IonicClient',
  webDir: 'dist',
  plugins: {
    GoogleMaps: {
        apiKey: mapsApiKey,
    },
},
};

export default config;
