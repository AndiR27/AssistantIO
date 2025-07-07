// src/app/shared/material-imports.ts
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatChip} from '@angular/material/chips';

/**
 * Tableau regroupant tous les modules Angular Material utilisés dans le projet.
 * Importez cette constante dans vos composants standalone pour gagner en concision.
 */
export const MATERIAL_MODULES = [
  MatCardModule,
  MatFormFieldModule,
  MatInputModule,
  MatSelectModule,
  MatButtonModule,
  MatIcon,
  MatChip
];
