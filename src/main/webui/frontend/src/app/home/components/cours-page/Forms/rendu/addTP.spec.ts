import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddTP } from './addTP';

describe('Rendu', () => {
  let component: AddTP;
  let fixture: ComponentFixture<AddTP>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddTP]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddTP);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
