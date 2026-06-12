package com.example.demo.service;

import com.example.demo.entity.History;
import com.example.demo.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {
	private final HistoryRepository historyRepository;

	public List<History> getAllHistory() {
		return historyRepository.findAll();
	}
}