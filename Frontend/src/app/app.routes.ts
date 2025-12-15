import { Routes } from '@angular/router';
import { HomeComponent } from './home/components/home/home.component';
import { CoursComponent } from './home/components/cours-page/cours.component';
import { NotFound } from './shared/components/not-found/not-found';
import { CourseResolver } from './shared/resolvers/course.resolver';
import { authGuard } from './shared/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'home/courses/:id', component: CoursComponent, resolve: { course: CourseResolver }, canActivate: [authGuard] },
  { path: '**', component: NotFound }
];


