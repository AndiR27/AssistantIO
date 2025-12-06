import { Injectable } from '@angular/core';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { StudentModel, TP_Model, CourseDetailsModel } from '../../../models/courseDetails.model';
import { StudentSubmissionType } from '../../../models/tpStatus.model';

@Injectable({
    providedIn: 'root'
})
export class ExportPdfComponent {

    /**
     * Generate PDF from table data with submission percentages
     * @param students Array of students
     * @param tps Array of TPs
     * @param threshold Percentage threshold for highlighting (e.g., 75)
     * @param course Optional course details for PDF title and filename
     */
    generatePDF(students: StudentModel[], tps: TP_Model[], threshold: number, course?: CourseDetailsModel): void {
        const doc = new jsPDF();

        // Build title with course details
        const courseTitle = (course && course.name && course.code && course.year_course)
            ? `${course.name} (${course.code}) - ${course.year_course}`
            : 'Tableau des Rendus';

        // Add title
        doc.setFontSize(18);
        doc.text(courseTitle, 14, 20);

        // Add threshold info
        doc.setFontSize(10);
        doc.text(`Seuil de réussite: ${threshold}%`, 14, 28);

        // Add legend for status icons
        doc.setFontSize(9);
        doc.text('Légende:', 14, 34);
        const legendItems = [
            { icon: '', label: 'Rendu', color: [200, 230, 201] },            // Just green color
            { icon: '', label: 'Retard', color: [255, 224, 178] },           // Just orange color
            { icon: '', label: 'Non rendu', color: [255, 205, 210] },        // Just red color
            { icon: '', label: 'Exempté', color: [207, 216, 220] },           // Just grey color
            { icon: '+', label: 'Bon', color: [200, 230, 201] },             // + for Good/Plus
            { icon: 'E', label: 'Vide', color: [255, 205, 210] },            // E for Empty (not submitted)
            { icon: '~', label: 'Médiocre', color: [255, 224, 178] },        // ~ for Mediocre
        ];

        let legendX = 50;
        let legendY = 32;
        const itemsPerRow = 4;

        legendItems.forEach((item, index) => {
            // Move to next row after 4 items
            if (index === itemsPerRow) {
                legendX = 50;
                legendY += 6; // Move down for second row
            }

            // Draw colored box
            doc.setFillColor(item.color[0], item.color[1], item.color[2]);
            doc.rect(legendX, legendY, 4, 4, 'F');
            // Draw icon (if any)
            if (item.icon) {
                doc.setTextColor(0, 0, 0);
                doc.text(item.icon, legendX + 0.5, legendY + 3.5);
            }
            // Draw label
            doc.setTextColor(0, 0, 0);
            doc.text(item.label, legendX + 6, legendY + 3.5);
            legendX += item.label.length * 2.5 + 12;
        });

        doc.setTextColor(0, 0, 0); // Reset text color

        // Prepare table headers
        const headers = ['Étudiant', ...tps.map(tp => `TP ${tp.no}`), '% Rendus'];

        // Prepare table data with icons instead of text
        // Also store status info for coloring
        const statusData: (StudentSubmissionType | null)[][] = [];

        const data = students
            .sort((a, b) => {
                const nameA = a.name?.toLowerCase() || '';
                const nameB = b.name?.toLowerCase() || '';
                return nameA.localeCompare(nameB);
            })
            .map(student => {
                const row: any[] = [student.name || 'N/A'];
                const statusRow: (StudentSubmissionType | null)[] = [];

                // Add TP statuses as icons
                tps.forEach(tp => {
                    const status = this.getTPStatusForStudent(tp, student.id);
                    statusRow.push(status);
                    row.push(this.getSubmissionStatusIcon(status));
                });

                // Calculate and add submission percentage
                const percentage = this.calculateSubmissionPercentage(student, tps);
                row.push(`${percentage.toFixed(0)}%`);

                statusData.push(statusRow);
                return row;
            });

        // Generate table with autoTable
        autoTable(doc, {
            head: [headers],
            body: data,
            startY: 48, // Moved down to accommodate two-row legend
            theme: 'grid',
            styles: {
                fontSize: 11, // Increased from 9 to 11
                cellPadding: 3,
                lineWidth: 0.3, // Normal border width for inner TP columns
                lineColor: [100, 100, 100],
                cellWidth: 'auto', // Auto width, will be overridden for specific columns
                minCellWidth: 15, // Minimum width for TP columns
            },
            headStyles: {
                fillColor: [63, 81, 181], // Material blue
                textColor: 255,
                fontStyle: 'bold',
                lineWidth: 0.75, // Thicker border for header
                halign: 'center', // Center all headers
            },
            columnStyles: {
                0: {
                    cellWidth: 40, // Student name column
                    halign: 'left' // Left-align student names in header too
                },
                [headers.length - 1]: {
                    halign: 'right', // Right-align percentage column
                    fontStyle: 'bold', // Bold percentage values
                    cellWidth: 25 // Wider to fit "% Rendus" on one line
                }
            },
            didDrawCell: (data) => {
                // Add bold borders to create visual sections
                const isFirstColumn = data.column.index === 0; // Student name
                const isLastTPColumn = data.column.index === headers.length - 2; // Last TP before percentage
                const isPercentageColumn = data.column.index === headers.length - 1; // Percentage column

                if (data.section === 'body' || data.section === 'head') {
                    // Bold horizontal borders (top and bottom) for all cells - separates students
                    doc.setLineWidth(0.5);
                    doc.setDrawColor(100, 100, 100);

                    // Top border
                    doc.line(
                        data.cell.x,
                        data.cell.y,
                        data.cell.x + data.cell.width,
                        data.cell.y
                    );

                    // Bottom border
                    doc.line(
                        data.cell.x,
                        data.cell.y + data.cell.height,
                        data.cell.x + data.cell.width,
                        data.cell.y + data.cell.height
                    );

                    // Bold left border for student name column
                    if (isFirstColumn) {
                        doc.setLineWidth(0.75);
                        doc.setDrawColor(100, 100, 100);
                        doc.line(
                            data.cell.x,
                            data.cell.y,
                            data.cell.x,
                            data.cell.y + data.cell.height
                        );
                    }

                    // Bold right border for last TP column (separates TPs from percentage)
                    if (isLastTPColumn) {
                        doc.setLineWidth(0.75);
                        doc.setDrawColor(100, 100, 100);
                        doc.line(
                            data.cell.x + data.cell.width,
                            data.cell.y,
                            data.cell.x + data.cell.width,
                            data.cell.y + data.cell.height
                        );
                    }

                    // Bold left border for percentage column (same as right of last TP)
                    if (isPercentageColumn) {
                        doc.setLineWidth(0.75);
                        doc.setDrawColor(100, 100, 100);
                        doc.line(
                            data.cell.x,
                            data.cell.y,
                            data.cell.x,
                            data.cell.y + data.cell.height
                        );
                    }
                }
            },
            didParseCell: (data) => {
                // Center align icons in TP columns
                if (data.column.index > 0 && data.column.index < headers.length - 1 && data.section === 'body') {
                    data.cell.styles.halign = 'center';
                }

                // Highlight percentage column based on threshold
                if (data.column.index === headers.length - 1 && data.section === 'body') {
                    const percentageText = data.cell.text[0];
                    const percentageValue = parseFloat(percentageText);

                    if (percentageValue >= threshold) {
                        data.cell.styles.fillColor = [200, 230, 201]; // Light green
                    } else {
                        data.cell.styles.fillColor = [255, 205, 210]; // Light red
                    }
                }

                // Color code submission status cells (TP columns) based on icon or status
                if (data.column.index > 0 && data.column.index < headers.length - 1 && data.section === 'body') {
                    const iconText = data.cell.text[0];
                    let color = this.getStatusColorByIcon(iconText);

                    // If icon is empty, get color based on status
                    if (!color && data.row.index < statusData.length) {
                        const status = statusData[data.row.index][data.column.index - 1];
                        color = this.getColorByStatus(status);
                    }

                    if (color) {
                        data.cell.styles.fillColor = color;
                    }
                }
            }
        });

        // Save the PDF with format: code_TauxRendus.pdf
        const fileName = course?.code
            ? `${course.code}_TauxRendus.pdf`
            : `TauxRendus_${new Date().toISOString().split('T')[0]}.pdf`;
        doc.save(fileName);
    }

    /**
     * Calculate submission percentage for a student
     * Excludes exempt TPs from both submitted count and total count
     * @param student Student to calculate for
     * @param tps Array of TPs
     * @returns Percentage of submitted TPs (0-100), excluding exempt TPs
     */
    private calculateSubmissionPercentage(student: StudentModel, tps: TP_Model[]): number {
        if (tps.length === 0) return 0;

        // Filter out exempt TPs for this student
        const nonExemptTPs = tps.filter(tp => {
            const status = this.getTPStatusForStudent(tp, student.id);
            return status !== StudentSubmissionType.EXEMPT;
        });

        // If all TPs are exempt, return 100%
        if (nonExemptTPs.length === 0) return 100;

        // Count submitted TPs (excluding exempt)
        const submittedCount = nonExemptTPs.filter(tp => {
            const status = this.getTPStatusForStudent(tp, student.id);
            return this.isSubmitted(status);
        }).length;

        return (submittedCount / nonExemptTPs.length) * 100;
    }

    /**
     * Get TP status for a specific student
     */
    private getTPStatusForStudent(tp: TP_Model, studentId: number | undefined): StudentSubmissionType | null {
        if (!tp.statusStudents || !studentId) {
            return null;
        }
        const status = tp.statusStudents.find(s => s.studentId === studentId);
        return status?.studentSubmission || null;
    }

    /**
     * Check if a status counts as "submitted"
     */
    private isSubmitted(status: StudentSubmissionType | null): boolean {
        if (!status) return false;

        return [
            StudentSubmissionType.DONE,
            StudentSubmissionType.DONE_LATE,
            StudentSubmissionType.DONE_GOOD,
            //StudentSubmissionType.DONE_BUT_NOTHING,
            StudentSubmissionType.DONE_BUT_MEDIOCRE
        ].includes(status);
    }

    /**
     * Convert status enum to readable text for PDF
     */
    private getSubmissionStatusText(status: StudentSubmissionType | null): string {
        if (!status) return 'N/A';

        switch (status) {
            case StudentSubmissionType.DONE:
                return 'Rendu';
            case StudentSubmissionType.DONE_LATE:
                return 'Retard';
            case StudentSubmissionType.DONE_GOOD:
                return 'Bon';
            case StudentSubmissionType.DONE_BUT_NOTHING:
                return 'Vide';
            case StudentSubmissionType.DONE_BUT_MEDIOCRE:
                return 'Médiocre';
            case StudentSubmissionType.NOT_DONE_MISSING:
                return 'Non rendu';
            case StudentSubmissionType.EXEMPT:
                return 'Exempté';
            default:
                return 'Inconnu';
        }
    }

    /**
     * Convert status enum to icon symbol for PDF
     */
    private getSubmissionStatusIcon(status: StudentSubmissionType | null): string {
        if (!status) return '';

        switch (status) {
            case StudentSubmissionType.DONE:
                return ''; // Just green color
            case StudentSubmissionType.DONE_LATE:
                return ''; // Just orange color
            case StudentSubmissionType.DONE_GOOD:
                return '+'; // Good/Plus
            case StudentSubmissionType.DONE_BUT_NOTHING:
                return 'E'; // Empty (not submitted)
            case StudentSubmissionType.DONE_BUT_MEDIOCRE:
                return '~'; // Mediocre
            case StudentSubmissionType.NOT_DONE_MISSING:
                return ''; // Just red color
            case StudentSubmissionType.EXEMPT:
                return ''; // Just grey color
            default:
                return '?';
        }
    }

    /**
     * Get color for status icon in PDF (RGB array)
     */
    private getStatusColorByIcon(icon: string): [number, number, number] | null {
        switch (icon) {
            case '': // Empty icon - need to check context, but we'll handle in didParseCell
                return null; // Will be set based on status
            case '+': // DONE_GOOD
                return [200, 230, 201]; // Light green
            case '~': // DONE_BUT_MEDIOCRE
                return [255, 224, 178]; // Light orange
            case 'E': // DONE_BUT_NOTHING (Empty - counts as not submitted)
                return [255, 205, 210]; // Light red
            default:
                return null;
        }
    }

    /**
     * Get color directly from status enum (for empty icons)
     */
    private getColorByStatus(status: StudentSubmissionType | null): [number, number, number] | null {
        if (!status) return null;

        switch (status) {
            case StudentSubmissionType.DONE:
                return [200, 230, 201]; // Green
            case StudentSubmissionType.DONE_LATE:
                return [255, 224, 178]; // Orange
            case StudentSubmissionType.DONE_GOOD:
                return [200, 230, 201]; // Green
            case StudentSubmissionType.DONE_BUT_NOTHING:
                return [255, 205, 210]; // Red
            case StudentSubmissionType.DONE_BUT_MEDIOCRE:
                return [255, 224, 178]; // Orange
            case StudentSubmissionType.NOT_DONE_MISSING:
                return [255, 205, 210]; // Red
            case StudentSubmissionType.EXEMPT:
                return [207, 216, 220]; // Grey
            default:
                return null;
        }
    }
}
