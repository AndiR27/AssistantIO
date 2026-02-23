import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MATERIAL_MODULES } from '../../../shared/imports/material-imports';
import { ApiService } from '../../../shared/services/api.service';
import { AuthService } from '../../../shared/services/auth.service';
import { CourseAccessModel, CoursePermission } from '../../models/courseAccess.model';

export interface ShareCourseDialogData {
  courseId: number;
  courseName: string;
}

@Component({
  selector: 'app-share-course-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, ...MATERIAL_MODULES],
  templateUrl: './share-course-dialog.component.html',
  styleUrl: './share-course-dialog.component.css'
})
export class ShareCourseDialogComponent implements OnInit {
  private api = inject(ApiService);
  private authService = inject(AuthService);
  private dialogRef = inject(MatDialogRef<ShareCourseDialogComponent>);
  data = inject<ShareCourseDialogData>(MAT_DIALOG_DATA);

  currentUserEmail = (this.authService.getIdentityClaims()?.['email'] as string) ?? '';

  shares: CourseAccessModel[] = [];
  isLoading = false;

  newEmail = '';
  newPermission: CoursePermission = 'READ';
  isSharing = false;
  revokingUserId: string | null = null;

  readonly permissions: CoursePermission[] = ['READ', 'WRITE', 'OWNER'];

  readonly permissionLabels: Record<CoursePermission, string> = {
    READ: 'Lecteur',
    WRITE: 'Éditeur',
    OWNER: 'Propriétaire',
  };

  ngOnInit(): void {
    this.loadShares();
  }

  loadShares(): void {
    this.isLoading = true;
    this.api.getCourseShares(this.data.courseId).subscribe({
      next: (shares) => {
        this.shares = shares;
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  isValidEmail(): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.newEmail.trim());
  }

  initials(email: string): string {
    return email.charAt(0).toUpperCase();
  }

  avatarColor(email: string): string {
    const palette = ['#0891b2', '#0d9488', '#7c3aed', '#db2777', '#ea580c', '#16a34a'];
    return palette[email.charCodeAt(0) % palette.length];
  }

  share(): void {
    const email = this.newEmail.trim();
    if (!this.isValidEmail()) return;

    this.isSharing = true;
    this.api.shareCourse(this.data.courseId, email, this.newPermission).subscribe({
      next: (newShare) => {
        this.shares = [...this.shares.filter(s => s.userId !== newShare.userId), newShare];
        this.newEmail = '';
        this.newPermission = 'READ';
        this.isSharing = false;
      },
      error: () => { this.isSharing = false; }
    });
  }

  revoke(share: CourseAccessModel): void {
    this.revokingUserId = share.userId;
    this.api.unshareCourse(this.data.courseId, share.userId).subscribe({
      next: () => {
        this.shares = this.shares.filter(s => s.userId !== share.userId);
        this.revokingUserId = null;
      },
      error: () => { this.revokingUserId = null; }
    });
  }

  isRevoking(share: CourseAccessModel): boolean {
    return this.revokingUserId === share.userId;
  }

  isOwnEntry(share: CourseAccessModel): boolean {
    return share.userId === this.currentUserEmail;
  }

  permissionClass(p: CoursePermission): string {
    return { READ: 'perm-read', WRITE: 'perm-write', OWNER: 'perm-owner' }[p];
  }

  close(): void {
    this.dialogRef.close();
  }
}