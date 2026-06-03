package com.personalbudgetmanager.summary;

import com.personalbudgetmanager.summary.dto.SummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/summary")
@RequiredArgsConstructor
class SummaryController {

    private final SummaryService summaryService;

    @GetMapping
    public ResponseEntity<SummaryResponse> getSummary() {
        return ResponseEntity.ok(summaryService.getSummary());
    }

}
