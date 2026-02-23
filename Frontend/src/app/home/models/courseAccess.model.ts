export type CoursePermission = 'READ' | 'WRITE' | 'OWNER';

export interface CourseAccessModel {
  id?: number;
  userId: string;
  courseId: number;
  permission: CoursePermission;
  createdAt?: string;
  updatedAt?: string;
}