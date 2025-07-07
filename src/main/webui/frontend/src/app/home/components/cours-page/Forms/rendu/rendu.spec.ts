import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Rendu } from './rendu';

describe('Rendu', () => {
  let component: Rendu;
  let fixture: ComponentFixture<Rendu>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Rendu]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Rendu);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
