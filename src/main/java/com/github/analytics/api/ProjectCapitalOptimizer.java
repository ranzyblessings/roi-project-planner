package com.github.analytics.api;

import com.github.analytics.exception.InvalidCapitalMaximizationQueryException;
import com.github.projects.model.ProjectDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Optimizes project selection to maximize final capital.
 * Uses a greedy algorithm to iteratively select the most profitable, affordable project.
 */
@Component
public class ProjectCapitalOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(ProjectCapitalOptimizer.class);

    /**
     * Optimizes project selection to maximize final capital.
     *
     * @param query The capital maximization query specifying available projects, maximum selections, and initial capital.
     * @return a {@code Mono} emitting a {@link ProjectCapitalOptimized} containing the selected projects and final capital.
     * @throws InvalidCapitalMaximizationQueryException if the query is null.
     */
    public Mono<ProjectCapitalOptimized> maximizeCapital(CapitalMaximizationQuery query) {
        if (query == null) {
            logger.error("Received null capital maximization query.");
            return Mono.error(new InvalidCapitalMaximizationQueryException("Query must not be null."));
        }

        logger.info("Starting capital maximization with initial capital: {} and {} available projects.",
                query.initialCapital(), query.availableProjects().size());

        // Offload the CPU-bound computation to a parallel scheduler.
        return Mono.fromCallable(() -> computeMaximizedCapital(query))
                .subscribeOn(Schedulers.parallel())
                .doOnSuccess(result -> logger.info("Capital maximization complete. Final capital: {}", result.finalCapital()))
                .doOnError(error -> logger.error("Error during capital maximization", error));
    }

    /**
     * Executes a greedy algorithm to maximize capital by iteratively selecting the most profitable affordable projects.
     *
     * @param query Query containing available projects, initial capital, and selection constraints.
     * @return a {@link ProjectCapitalOptimized} with the selected projects and final capital.
     */
    private ProjectCapitalOptimized computeMaximizedCapital(CapitalMaximizationQuery query) {
        List<ProjectDTO> projects = new ArrayList<>(query.availableProjects());

        // Log total projects available.
        int totalProjects = projects.size();
        logger.info("Starting capital maximization with {} available projects and initial capital: {}",
                totalProjects, query.initialCapital());

        // Sort projects by required capital in ascending order.
        projects.sort(Comparator.comparing(ProjectDTO::requiredCapital));
        logger.info("Projects sorted by required capital.");

        // Max-heap (priority queue) to track the most profitable affordable projects.
        PriorityQueue<ProjectDTO> profitMaxHeap = new PriorityQueue<>(Comparator.comparing(ProjectDTO::profit).reversed());

        List<ProjectDTO> selectedProjects = new ArrayList<>();
        BigDecimal currentCapital = query.initialCapital();
        int projectIndex = 0;

        // Iteratively select up to maxProjects.
        for (int i = 0; i < query.maxProjects(); i++) {
            logger.info("Iteration {}: Current capital: {}", i + 1, currentCapital);

            // Add all affordable projects to the max-heap.
            while (projectIndex < totalProjects && projects.get(projectIndex).requiredCapital().compareTo(currentCapital) <= 0) {
                ProjectDTO project = projects.get(projectIndex);
                profitMaxHeap.offer(project);
                logger.info("Added project {} to heap (Required: {}, Profit: {}).",
                        project.name(), project.requiredCapital(), project.profit());
                projectIndex++;
            }

            // If no affordable projects remain, exit early.
            if (profitMaxHeap.isEmpty()) {
                logger.info("No further projects can be selected with current capital: {}", currentCapital);
                break;
            }

            // Select the most profitable project.
            ProjectDTO chosenProject = profitMaxHeap.poll();
            selectedProjects.add(chosenProject);
            currentCapital = currentCapital.add(chosenProject.profit());
            logger.info("Selected project {} (Profit: {}). Updated capital: {}",
                    chosenProject.name(), chosenProject.profit(), currentCapital);
        }

        return new ProjectCapitalOptimized(selectedProjects, currentCapital);
    }
}