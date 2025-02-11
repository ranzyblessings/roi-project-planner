package com.github.analytics;

import com.github.projects.model.ProjectDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Optimizes project selection for maximum final capital.
 * Employs a greedy algorithm, iteratively selecting the most profitable, affordable project.
 */
public final class ProjectCapitalOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(ProjectCapitalOptimizer.class);

    /**
     * Maximizes the final capital based on the provided query.
     *
     * @param query the capital maximization query containing available projects, max selections, and initial capital.
     * @return a {@code Mono} emitting a {@link ProjectCapitalOptimized} containing the selected projects and final capital.
     * @throws IllegalArgumentException if the query or its available projects list is null.
     */
    public Mono<ProjectCapitalOptimized> maximizeCapital(CapitalMaximizationQuery query) {
        if (query == null || query.availableProjects() == null) {
            logger.error("Received null query or available projects list.");
            return Mono.error(new IllegalArgumentException("Capital maximization query must not be null"));
        }

        logger.info("Starting capital maximization with initial capital: {}", query.initialCapital());

        // Offload the CPU-bound computation to a parallel scheduler.
        return Mono.fromCallable(() -> computeMaximizedCapital(query))
                .subscribeOn(Schedulers.parallel())
                .doOnSuccess(result -> logger.info("Capital maximization complete. Final capital: {}", result.finalCapital()))
                .doOnError(error -> logger.error("Error during capital maximization", error));
    }

    /**
     * Performs the greedy algorithm to select projects and maximize capital.
     *
     * @param query the capital maximization query.
     * @return a {@link ProjectCapitalOptimized} with the selected projects and final capital.
     */
    private ProjectCapitalOptimized computeMaximizedCapital(CapitalMaximizationQuery query) {
        List<ProjectDTO> projects = new ArrayList<>(query.availableProjects());
        logger.debug("Number of available projects: {}", projects.size());

        // Sort projects by required capital in ascending order.
        projects.sort(Comparator.comparing(ProjectDTO::requiredCapital));
        logger.debug("Projects sorted by required capital.");

        // Max-heap to choose the project with the highest profit among those affordable.
        var profitMaxHeap = new PriorityQueue<>(Comparator.comparing(ProjectDTO::profit).reversed());

        List<ProjectDTO> selectedProjects = new ArrayList<>();
        BigDecimal currentCapital = query.initialCapital();
        int totalProjects = projects.size();
        int projectIndex = 0;

        for (int i = 0; i < query.maxProjects(); i++) {
            // Log the current iteration and capital.
            logger.debug("Iteration {}: Current capital: {}", i, currentCapital);

            // Add all projects whose required capital is within the current capital.
            while (projectIndex < totalProjects
                    && projects.get(projectIndex).requiredCapital().compareTo(currentCapital) <= 0) {
                ProjectDTO project = projects.get(projectIndex);
                profitMaxHeap.offer(project);
                logger.debug("Project {} (profit: {}) is affordable and added to the heap.", project.name(), project.profit());
                projectIndex++;
            }

            // If no projects are available to start, break early.
            if (profitMaxHeap.isEmpty()) {
                logger.info("No further projects can be selected with current capital: {}", currentCapital);
                break;
            }

            // Select the project with the highest profit.
            ProjectDTO chosenProject = profitMaxHeap.poll();
            selectedProjects.add(chosenProject);
            currentCapital = currentCapital.add(chosenProject.profit());
            logger.info("Selected project {}. Updated capital: {}", chosenProject.name(), currentCapital);
        }

        return new ProjectCapitalOptimized(selectedProjects, currentCapital);
    }
}