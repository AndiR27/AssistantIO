import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluationCourse } from './evaluation-course';

describe('EvaluationCourse', () => {
  let component: EvaluationCourse;
  let fixture: ComponentFixture<EvaluationCourse>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluationCourse]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluationCourse);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
