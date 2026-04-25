package com.aitutor.api.assignment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "assignment_plan_steps")
public class AssignmentPlanStep {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID assignmentId;

    @Column(nullable = false)
    private int stepOrder;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int plannedStartMinute;

    @Column(nullable = false)
    private int plannedEndMinute;

    protected AssignmentPlanStep() {
    }

    public AssignmentPlanStep(UUID assignmentId, int stepOrder, String title, int plannedStartMinute, int plannedEndMinute) {
        this.id = UUID.randomUUID();
        this.assignmentId = assignmentId;
        this.stepOrder = stepOrder;
        this.title = title;
        this.plannedStartMinute = plannedStartMinute;
        this.plannedEndMinute = plannedEndMinute;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public String getTitle() {
        return title;
    }

    public int getPlannedStartMinute() {
        return plannedStartMinute;
    }

    public int getPlannedEndMinute() {
        return plannedEndMinute;
    }
}
