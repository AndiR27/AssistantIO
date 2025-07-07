import { Routes } from '@angular/router';
import {HomeComponent} from './home/components/home/home.component';
import {CoursComponent} from './home/components/cours-page/cours.component';
import {NotFound} from './shared/components/not-found/not-found';
import {CourseResolver} from './shared/resolvers/course.resolver';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  {path :  'home/courses/:id', component : CoursComponent, resolve :{course : CourseResolver}},
  {path: '**', component: NotFound } // Redirect any unknown paths to home
];
