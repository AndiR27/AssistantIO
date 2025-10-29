import { bootstrapApplication } from '@angular/platform-browser';
import { config } from './app/app.config.server';
import {HomeComponent} from './app/home/components/home/home.component';
import {AppComponent} from './app/app.component';

const bootstrap = () => bootstrapApplication(AppComponent, config);

export default bootstrap;
