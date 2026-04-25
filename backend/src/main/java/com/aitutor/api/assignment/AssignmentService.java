package com.aitutor.api.assignment;

import com.aitutor.api.auth.Student;
import com.aitutor.api.auth.StudentRepository;
import com.aitutor.api.config.AppConstants;
import com.aitutor.api.storage.StorageService;
import com.aitutor.api.storage.StoredFile;
import com.aitutor.api.worker.AssignmentAnalysisCommand;
import com.aitutor.api.worker.AssignmentAnalysisResult;
import com.aitutor.api.worker.PlanStepResult;
import com.aitutor.api.worker.WorkerClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AssignmentService {

    private final AssignmentRepository assignments;
    private final AssignmentAssetRepository assets;
    private final AssignmentPlanStepRepository steps;
    private final StudentRepository students;
    private final StorageService storageService;
    private final WorkerClient workerClient;

    public AssignmentService(AssignmentRepository assignments, AssignmentAssetRepository assets,
                             AssignmentPlanStepRepository steps, StudentRepository students,
                             StorageService storageService, WorkerClient workerClient) {
        this.assignments = assignments;
        this.assets = assets;
        this.steps = steps;
        this.students = students;
        this.storageService = storageService;
        this.workerClient = workerClient;
    }

    @Transactional
    public AssignmentResponse create(UUID studentId, CreateAssignmentRequest request) {
        Assignment assignment = assignments.save(new Assignment(studentId, normalizeSubject(request.subject())));
        return response(assignment);
    }

    public List<String> subjects() {
        return AppConstants.SUBJECTS.stream()
                .sorted()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> list(UUID studentId) {
        return assignments.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssignmentResponse get(UUID studentId, UUID assignmentId) {
        return response(findOwned(studentId, assignmentId));
    }

    @Transactional
    public AssignmentResponse uploadHomeworkImage(UUID studentId, UUID assignmentId, MultipartFile file) {
        Assignment assignment = findOwned(studentId, assignmentId);
        StoredFile stored = storageService.save(file, studentId, AppConstants.STORAGE_CATEGORY_ASSIGNMENTS);
        assets.save(new AssignmentAsset(
                assignment.getId(),
                AppConstants.ASSET_TYPE_HOMEWORK_IMAGE,
                stored.objectKey(),
                stored.filePath(),
                stored.contentType(),
                stored.sizeBytes()
        ));
        assignment.markImageUploaded();
        return response(assignment);
    }

    @Transactional
    public AssignmentResponse analyze(UUID studentId, UUID assignmentId) {
        Assignment assignment = findOwned(studentId, assignmentId);
        Student student = students.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        AssignmentAsset image = assets.findFirstByAssignmentIdAndAssetTypeOrderByCreatedAtDesc(
                        assignmentId,
                        AppConstants.ASSET_TYPE_HOMEWORK_IMAGE
                )
                .orElseThrow(() -> new IllegalStateException("Upload a homework image before analysis"));

        AssignmentAnalysisResult result = workerClient.analyzeAssignment(new AssignmentAnalysisCommand(
                assignment.getId().toString(),
                image.getFilePath(),
                assignment.getSubject(),
                student.getGradeLevel(),
                student.getAge()
        ));

        assignment.applyAnalysis(
                result.subject(),
                result.taskType(),
                result.estimatedWordCount(),
                result.questionCount(),
                result.estimatedTotalMinutes(),
                result.confidence(),
                result.summary()
        );

        steps.deleteByAssignmentId(assignment.getId());
        for (PlanStepResult step : result.steps()) {
            steps.save(new AssignmentPlanStep(
                    assignment.getId(),
                    step.stepOrder(),
                    step.title(),
                    step.plannedStartMinute(),
                    step.plannedEndMinute()
            ));
        }

        return response(assignment);
    }

    public Assignment findOwned(UUID studentId, UUID assignmentId) {
        Assignment assignment = assignments.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        if (!assignment.getStudentId().equals(studentId)) {
            throw new IllegalArgumentException("Assignment not found");
        }
        return assignment;
    }

    public AssignmentResponse response(Assignment assignment) {
        List<AssignmentPlanStepResponse> planSteps = steps.findByAssignmentIdOrderByStepOrder(assignment.getId()).stream()
                .map(step -> new AssignmentPlanStepResponse(
                        step.getStepOrder(),
                        step.getTitle(),
                        step.getPlannedStartMinute(),
                        step.getPlannedEndMinute()
                ))
                .toList();

        return new AssignmentResponse(
                assignment.getId().toString(),
                assignment.getStatus().name(),
                assignment.getSubject(),
                assignment.getTaskType(),
                assignment.getEstimatedWordCount(),
                assignment.getQuestionCount(),
                assignment.getEstimatedTotalMinutes(),
                assignment.getConfidence(),
                assignment.getSummary(),
                planSteps
        );
    }

    private String normalizeSubject(String rawSubject) {
        String subject = rawSubject == null ? "" : rawSubject.trim().toUpperCase(Locale.ROOT);
        if (!AppConstants.SUBJECTS.contains(subject)) {
            throw new IllegalArgumentException("Subject must be one of: " + String.join(", ", subjects()));
        }
        return subject;
    }
}
