// src/app/resolvers/course.resolver.ts

import { Injectable } from '@angular/core';
import {
  Resolve,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router
} from '@angular/router';
import {Observable, EMPTY, tap} from 'rxjs';
import { catchError } from 'rxjs/operators';

import {CourseDetailsModel} from '../../home/models/courseDetails.model';
import {CourseService} from '../../home/services/course.service';

@Injectable({
  providedIn: 'root'
})
export class CourseResolver implements Resolve<CourseDetailsModel> {

  constructor(
    private courseService: CourseService,
    private router: Router
  ) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<CourseDetailsModel> {
    const id = Number(route.paramMap.get('id'));
    return this.courseService.getCourseById(id);

  }
}
