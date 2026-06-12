package com.example.demo.controller;

import com.example.demo.entity.History;
import com.example.demo.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class HistoryController {
	private final HistoryService historyService;

	@GetMapping
	public ResponseEntity<List<History>> getAllHistory() {
		return ResponseEntity.ok(historyService.getAllHistory());
	}
}