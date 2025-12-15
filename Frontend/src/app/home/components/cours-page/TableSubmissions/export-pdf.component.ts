import { Injectable } from '@angular/core';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { StudentModel, TP_Model, CourseDetailsModel } from '../../../models/courseDetails.model';
import { StudentSubmissionType } from '../../../models/tpStatus.model';

@Injectable({
    providedIn: 'root'
})
export class ExportPdfComponent {

    // ============================================
    // GÉNÉRATION DU PDF PRINCIPAL
    // ============================================

    /**
     * Génère un PDF avec les données du tableau et les pourcentages de soumission
     * @param students Liste des étudiants
     * @param tps Liste des TPs
     * @param threshold Seuil de pourcentage pour la mise en évidence (ex: 75)
     * @param course Détails du cours (optionnel) pour le titre et le nom du fichier
     */
    generatePDF(students: StudentModel[], tps: TP_Model[], threshold: number, course?: CourseDetailsModel): void {
        // Use landscape orientation if more than 8 TPs to fit all columns
        const orientation = tps.length > 8 ? 'landscape' : 'portrait';
        const doc = new jsPDF({ orientation });

        this.addPdfHeader(doc, threshold, course);
        this.addLegend(doc);

        const headers = this.buildHeaders(tps);
        const { data, statusData } = this.buildTableData(students, tps);

        this.generateTable(doc, headers, data, statusData, threshold);
        this.savePdf(doc, course);
    }

    // ============================================
    // EN-TÊTE DU PDF
    // ============================================

    /**
     * Ajoute l'en-tête du PDF avec le titre du cours et le seuil de réussite
     * @param doc Document jsPDF
     * @param threshold Seuil de réussite en pourcentage
     * @param course Détails du cours (optionnel)
     */
    private addPdfHeader(doc: jsPDF, threshold: number, course?: CourseDetailsModel): void {
        const courseTitle = (course && course.name && course.code && course.year_course)
            ? `${course.name} (${course.code}) - ${course.year_course}`
            : 'Tableau des Rendus';

        doc.setFontSize(18);
        doc.text(courseTitle, 14, 20);

        doc.setFontSize(10);
        doc.text(`Seuil de réussite: ${threshold}%`, 14, 28);
    }

    // ============================================
    // LÉGENDE DES STATUTS
    // ============================================

    /**
     * Ajoute la légende des statuts avec les couleurs et icônes
     * @param doc Document jsPDF
     */
    private addLegend(doc: jsPDF): void {
        doc.setFontSize(9);
        doc.text('Légende:', 14, 34);

        const legendItems = [
            { icon: '', label: 'Rendu', color: [200, 230, 201] as [number, number, number] },
            { icon: '', label: 'Retard', color: [255, 224, 178] as [number, number, number] },
            { icon: '', label: 'Non rendu', color: [255, 205, 210] as [number, number, number] },
            { icon: '', label: 'Exempté', color: [207, 216, 220] as [number, number, number] },
            { icon: '+', label: 'Bon', color: [200, 230, 201] as [number, number, number] },
            { icon: 'E', label: 'Vide', color: [255, 205, 210] as [number, number, number] },
            { icon: '~', label: 'Médiocre', color: [255, 224, 178] as [number, number, number] },
        ];

        this.renderLegendItems(doc, legendItems);
        doc.setTextColor(0, 0, 0);
    }

    /**
     * Affiche les éléments de la légende sur deux lignes (4 par ligne)
     * @param doc Document jsPDF
     * @param items Liste des éléments de légende
     */
    private renderLegendItems(doc: jsPDF, items: Array<{ icon: string, label: string, color: [number, number, number] }>): void {
        let legendX = 50;
        let legendY = 32;
        const itemsPerRow = 4;

        items.forEach((item, index) => {
            if (index === itemsPerRow) {
                legendX = 50;
                legendY += 6;
            }

            doc.setFillColor(item.color[0], item.color[1], item.color[2]);
            doc.rect(legendX, legendY, 4, 4, 'F');

            if (item.icon) {
                doc.setTextColor(0, 0, 0);
                doc.text(item.icon, legendX + 0.5, legendY + 3.5);
            }

            doc.setTextColor(0, 0, 0);
            doc.text(item.label, legendX + 6, legendY + 3.5);
            legendX += item.label.length * 2.5 + 12;
        });
    }

    // ============================================
    // CONSTRUCTION DES DONNÉES DU TABLEAU
    // ============================================

    /**
     * Construit les en-têtes du tableau (Étudiant, TP1, TP2, ..., % Rendus)
     * @param tps Liste des TPs
     * @returns Tableau des en-têtes
     */
    private buildHeaders(tps: TP_Model[]): string[] {
        return ['Étudiant', ...tps.map(tp => `TP ${tp.no}`), '% Rendus'];
    }

    /**
     * Construit les données du tableau avec les icônes de statut et les pourcentages
     * Stocke également les statuts pour l'application des couleurs
     * @param students Liste des étudiants
     * @param tps Liste des TPs
     * @returns Objet contenant les données du tableau et les statuts
     */
    private buildTableData(students: StudentModel[], tps: TP_Model[]): { data: any[][], statusData: (StudentSubmissionType | null)[][] } {
        const statusData: (StudentSubmissionType | null)[][] = [];

        const data = students
            .sort((a, b) => (a.name?.toLowerCase() || '').localeCompare(b.name?.toLowerCase() || ''))
            .map(student => {
                const row: any[] = [student.name || 'N/A'];
                const statusRow: (StudentSubmissionType | null)[] = [];

                tps.forEach(tp => {
                    const status = this.getTPStatusForStudent(tp, student.id);
                    statusRow.push(status);
                    row.push(this.getSubmissionStatusIcon(status));
                });

                const percentage = this.calculateSubmissionPercentage(student, tps);
                row.push(`${percentage.toFixed(0)}%`);

                statusData.push(statusRow);
                return row;
            });

        return { data, statusData };
    }

    // ============================================
    // GÉNÉRATION DU TABLEAU AVEC AUTOTABLE
    // ============================================

    /**
     * Génère le tableau avec autoTable et applique les styles personnalisés
     * @param doc Document jsPDF
     * @param headers En-têtes du tableau
     * @param data Données du tableau
     * @param statusData Statuts pour l'application des couleurs
     * @param threshold Seuil de réussite
     */
    private generateTable(doc: jsPDF, headers: string[], data: any[][], statusData: (StudentSubmissionType | null)[][], threshold: number): void {
        autoTable(doc, {
            head: [headers],
            body: data,
            startY: 48,
            theme: 'grid',
            styles: {
                fontSize: 10,
                cellPadding: 2,
                lineWidth: 0.3,
                lineColor: [100, 100, 100],
                cellWidth: 'auto',
                minCellWidth: 12,
            },
            headStyles: {
                fillColor: [63, 81, 181],
                textColor: 255,
                fontStyle: 'bold',
                lineWidth: 0.75,
                halign: 'center',
            },
            columnStyles: {
                0: {
                    cellWidth: 40,
                    halign: 'left'
                },
                [headers.length - 1]: {
                    halign: 'right',
                    fontStyle: 'bold',
                    cellWidth: 25
                }
            },
            didDrawCell: (cellData) => this.customizeCellBorders(doc, cellData, headers),
            didParseCell: (cellData) => this.applyCellColors(cellData, headers, statusData, threshold)
        });
    }

    // ============================================
    // PERSONNALISATION DES BORDURES
    // ============================================

    /**
     * Personnalise les bordures des cellules pour créer des sections visuelles
     * @param doc Document jsPDF
     * @param cellData Données de la cellule
     * @param headers En-têtes du tableau
     */
    private customizeCellBorders(doc: jsPDF, cellData: any, headers: string[]): void {
        const isFirstColumn = cellData.column.index === 0;
        const isLastTPColumn = cellData.column.index === headers.length - 2;
        const isPercentageColumn = cellData.column.index === headers.length - 1;

        if (cellData.section === 'body' || cellData.section === 'head') {
            this.drawHorizontalBorders(doc, cellData);

            if (isFirstColumn) {
                this.drawVerticalBorder(doc, cellData, 'left');
            }
            if (isLastTPColumn) {
                this.drawVerticalBorder(doc, cellData, 'right');
            }
            if (isPercentageColumn) {
                this.drawVerticalBorder(doc, cellData, 'left');
            }
        }
    }

    /**
     * Dessine les bordures horizontales (haut et bas) pour séparer les lignes
     * @param doc Document jsPDF
     * @param cellData Données de la cellule
     */
    private drawHorizontalBorders(doc: jsPDF, cellData: any): void {
        doc.setLineWidth(0.5);
        doc.setDrawColor(100, 100, 100);

        doc.line(cellData.cell.x, cellData.cell.y, cellData.cell.x + cellData.cell.width, cellData.cell.y);
        doc.line(cellData.cell.x, cellData.cell.y + cellData.cell.height, cellData.cell.x + cellData.cell.width, cellData.cell.y + cellData.cell.height);
    }

    /**
     * Dessine une bordure verticale épaisse pour séparer les sections
     * @param doc Document jsPDF
     * @param cellData Données de la cellule
     * @param side Côté de la bordure ('left' ou 'right')
     */
    private drawVerticalBorder(doc: jsPDF, cellData: any, side: 'left' | 'right'): void {
        doc.setLineWidth(0.75);
        doc.setDrawColor(100, 100, 100);

        const x = side === 'left' ? cellData.cell.x : cellData.cell.x + cellData.cell.width;
        doc.line(x, cellData.cell.y, x, cellData.cell.y + cellData.cell.height);
    }

    // ============================================
    // APPLICATION DES COULEURS
    // ============================================

    /**
     * Applique les couleurs aux cellules selon leur contenu et statut
     * @param cellData Données de la cellule
     * @param headers En-têtes du tableau
     * @param statusData Statuts pour l'application des couleurs
     * @param threshold Seuil de réussite
     */
    private applyCellColors(cellData: any, headers: string[], statusData: (StudentSubmissionType | null)[][], threshold: number): void {
        if (cellData.column.index > 0 && cellData.column.index < headers.length - 1 && cellData.section === 'body') {
            cellData.cell.styles.halign = 'center';
        }

        if (cellData.column.index === headers.length - 1 && cellData.section === 'body') {
            this.applyPercentageColor(cellData, threshold);
        }

        if (cellData.column.index > 0 && cellData.column.index < headers.length - 1 && cellData.section === 'body') {
            this.applyStatusColor(cellData, statusData);
        }
    }

    /**
     * Applique la couleur à la colonne de pourcentage selon le seuil
     * @param cellData Données de la cellule
     * @param threshold Seuil de réussite
     */
    private applyPercentageColor(cellData: any, threshold: number): void {
        const percentageValue = parseFloat(cellData.cell.text[0]);
        cellData.cell.styles.fillColor = percentageValue >= threshold
            ? [200, 230, 201]
            : [255, 205, 210];
    }

    /**
     * Applique la couleur aux cellules de statut selon l'icône ou le statut
     * @param cellData Données de la cellule
     * @param statusData Statuts pour l'application des couleurs
     */
    private applyStatusColor(cellData: any, statusData: (StudentSubmissionType | null)[][]): void {
        const iconText = cellData.cell.text[0];
        let color = this.getStatusColorByIcon(iconText);

        if (!color && cellData.row.index < statusData.length) {
            const status = statusData[cellData.row.index][cellData.column.index - 1];
            color = this.getColorByStatus(status);
        }

        if (color) {
            cellData.cell.styles.fillColor = color;
        }
    }

    // ============================================
    // SAUVEGARDE DU PDF
    // ============================================

    /**
     * Sauvegarde le PDF avec un nom de fichier formaté
     * @param doc Document jsPDF
     * @param course Détails du cours (optionnel)
     */
    private savePdf(doc: jsPDF, course?: CourseDetailsModel): void {
        const fileName = course?.code
            ? `${course.code}_TauxRendus.pdf`
            : `TauxRendus_${new Date().toISOString().split('T')[0]}.pdf`;
        doc.save(fileName);
    }

    // ============================================
    // CALCUL DU POURCENTAGE DE SOUMISSION
    // ============================================

    /**
     * Calcule le pourcentage de soumission pour un étudiant
     * Exclut les TPs exemptés du calcul (numérateur et dénominateur)
     * @param student Étudiant concerné
     * @param tps Liste des TPs
     * @returns Pourcentage de TPs soumis (0-100)
     */
    private calculateSubmissionPercentage(student: StudentModel, tps: TP_Model[]): number {
        if (tps.length === 0) return 0;

        const nonExemptTPs = tps.filter(tp => {
            const status = this.getTPStatusForStudent(tp, student.id);
            return status !== StudentSubmissionType.EXEMPT;
        });

        if (nonExemptTPs.length === 0) return 100;

        const submittedCount = nonExemptTPs.filter(tp => {
            const status = this.getTPStatusForStudent(tp, student.id);
            return this.isSubmitted(status);
        }).length;

        return (submittedCount / nonExemptTPs.length) * 100;
    }

    // ============================================
    // UTILITAIRES DE STATUT
    // ============================================

    /**
     * Récupère le statut d'un TP pour un étudiant spécifique
     * @param tp TP concerné
     * @param studentId ID de l'étudiant
     * @returns Statut du TP ou null
     */
    private getTPStatusForStudent(tp: TP_Model, studentId: number | undefined): StudentSubmissionType | null {
        if (!tp.statusStudents || !studentId) return null;
        const status = tp.statusStudents.find(s => s.studentId === studentId);
        return status?.studentSubmission || null;
    }

    /**
     * Vérifie si un statut compte comme "soumis"
     * @param status Statut à vérifier
     * @returns true si le statut compte comme soumis
     */
    private isSubmitted(status: StudentSubmissionType | null): boolean {
        if (!status) return false;
        return [
            StudentSubmissionType.DONE,
            StudentSubmissionType.DONE_LATE,
            StudentSubmissionType.DONE_GOOD,
            StudentSubmissionType.DONE_BUT_MEDIOCRE
        ].includes(status);
    }

    /**
     * Convertit un statut en icône pour le PDF
     * @param status Statut à convertir
     * @returns Icône correspondante (ou chaîne vide)
     */
    private getSubmissionStatusIcon(status: StudentSubmissionType | null): string {
        if (!status) return '';

        switch (status) {
            case StudentSubmissionType.DONE: return '';
            case StudentSubmissionType.DONE_LATE: return '';
            case StudentSubmissionType.DONE_GOOD: return '+';
            case StudentSubmissionType.DONE_BUT_NOTHING: return 'E';
            case StudentSubmissionType.DONE_BUT_MEDIOCRE: return '~';
            case StudentSubmissionType.NOT_DONE_MISSING: return '';
            case StudentSubmissionType.EXEMPT: return '';
            default: return '?';
        }
    }

    // ============================================
    // MAPPING DES COULEURS
    // ============================================

    /**
     * Récupère la couleur associée à une icône
     * @param icon Icône à traiter
     * @returns Couleur RGB ou null
     */
    private getStatusColorByIcon(icon: string): [number, number, number] | null {
        switch (icon) {
            case '': return null;
            case '+': return [200, 230, 201];
            case '~': return [255, 224, 178];
            case 'E': return [255, 205, 210];
            default: return null;
        }
    }

    /**
     * Récupère la couleur directement depuis le statut (pour les icônes vides)
     * @param status Statut à traiter
     * @returns Couleur RGB ou null
     */
    private getColorByStatus(status: StudentSubmissionType | null): [number, number, number] | null {
        if (!status) return null;

        switch (status) {
            case StudentSubmissionType.DONE: return [200, 230, 201];
            case StudentSubmissionType.DONE_LATE: return [255, 224, 178];
            case StudentSubmissionType.DONE_GOOD: return [200, 230, 201];
            case StudentSubmissionType.DONE_BUT_NOTHING: return [255, 205, 210];
            case StudentSubmissionType.DONE_BUT_MEDIOCRE: return [255, 224, 178];
            case StudentSubmissionType.NOT_DONE_MISSING: return [255, 205, 210];
            case StudentSubmissionType.EXEMPT: return [207, 216, 220];
            default: return null;
        }
    }
}
