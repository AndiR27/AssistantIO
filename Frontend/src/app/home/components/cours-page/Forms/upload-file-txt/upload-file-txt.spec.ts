import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UploadFileTxt } from './upload-file-txt';

describe('UploadFileTxt', () => {
  let component: UploadFileTxt;
  let fixture: ComponentFixture<UploadFileTxt>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UploadFileTxt]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UploadFileTxt);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
