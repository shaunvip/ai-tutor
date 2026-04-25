package com.aitutor.api.tutor;

import com.aitutor.api.common.CurrentStudent;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tutor")
public class TutorController {

    private final TutorService tutorService;
    private final CurrentStudent currentStudent;

    public TutorController(TutorService tutorService, CurrentStudent currentStudent) {
        this.tutorService = tutorService;
        this.currentStudent = currentStudent;
    }

    @PostMapping("/threads")
    TutorThreadResponse createThread(@RequestBody CreateTutorThreadRequest request) {
        return tutorService.createThread(currentStudent.id(), request);
    }

    @GetMapping("/threads")
    List<TutorThreadResponse> listThreads() {
        return tutorService.listThreads(currentStudent.id());
    }

    @PostMapping("/threads/{threadId}/messages")
    TutorMessageResponse sendMessage(@PathVariable UUID threadId, @RequestBody SendTutorMessageRequest request) {
        return tutorService.sendMessage(currentStudent.id(), threadId, request);
    }
}
