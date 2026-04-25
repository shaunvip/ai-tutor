package com.aitutor.api.assignment;

import com.aitutor.api.common.CurrentStudent;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final CurrentStudent currentStudent;

    public AssignmentController(AssignmentService assignmentService, CurrentStudent currentStudent) {
        this.assignmentService = assignmentService;
        this.currentStudent = currentStudent;
    }

    @PostMapping
    AssignmentResponse create(@Valid @RequestBody CreateAssignmentRequest request) {
        return assignmentService.create(currentStudent.id(), request);
    }

    @GetMapping
    List<AssignmentResponse> list() {
        return assignmentService.list(currentStudent.id());
    }

    @GetMapping("/subjects")
    List<String> subjects() {
        return assignmentService.subjects();
    }

    @GetMapping("/{assignmentId}")
    AssignmentResponse get(@PathVariable UUID assignmentId) {
        return assignmentService.get(currentStudent.id(), assignmentId);
    }

    @PostMapping(value = "/{assignmentId}/homework-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    AssignmentResponse uploadHomeworkImage(@PathVariable UUID assignmentId, @RequestPart("file") MultipartFile file) {
        return assignmentService.uploadHomeworkImage(currentStudent.id(), assignmentId, file);
    }

    @PostMapping("/{assignmentId}/analyze")
    AssignmentResponse analyze(@PathVariable UUID assignmentId) {
        return assignmentService.analyze(currentStudent.id(), assignmentId);
    }
}
