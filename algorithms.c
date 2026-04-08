#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include "algorithms.h"

double min_d(double a, double b) {
	return (a < b) ? a : b;
}
void algo_1_fruchterman_reingold(Graph* g, double width, double height, int iterations) {
	if (g->vertex_count == 0) return;
	srand((unsigned int)time(NULL));
	for (int i = 0; i < g->vertex_count; i++) {
		g->vertices[i].x = ((double)rand() / RAND_MAX) * width;
		g->vertices[i].y = ((double)rand() / RAND_MAX) * height;
	}
	double area = width * height;
	double k = sqrt(area / g->vertex_count);
	double t = width / 10.0;
	for (int iter = 0; iter < iterations; iter++) {
		for (int i = 0; i < g->vertex_count; i++) {
			g->vertices[i].dx = 0.0;
			g->vertices[i].dy = 0.0;
		}
		for (int v = 0; v < g->vertex_count; v++) {
			for (int u = 0; u < g->vertex_count; u++) {
				if (v == u) continue; 

				double delta_x = g->vertices[v].x - g->vertices[u].x;
				double delta_y = g->vertices[v].y - g->vertices[u].y;
				double distance = sqrt(delta_x * delta_x + delta_y * delta_y);

				if (distance > 0.0001) { 
					double repulsion_force = (k * k) / distance;
					g->vertices[v].dx += (delta_x / distance) * repulsion_force;
					g->vertices[v].dy += (delta_y / distance) * repulsion_force;
				}
			}
	}
		for (int e = 0; e < g->count; e++) {
			
			int v_idx = get_or_add_vertex(g, g->edges[e].v1);
			int u_idx = get_or_add_vertex(g, g->edges[e].v2);

			double delta_x = g->vertices[v_idx].x - g->vertices[u_idx].x;
			double delta_y = g->vertices[v_idx].y - g->vertices[u_idx].y;
			double distance = sqrt(delta_x * delta_x + delta_y * delta_y);

			if (distance > 0.0001) {
				double attraction_force = (distance * distance) / k;
				g->vertices[v_idx].dx -= (delta_x / distance) * attraction_force;
				g->vertices[v_idx].dy -= (delta_y / distance) * attraction_force;
				g->vertices[u_idx].dx += (delta_x / distance) * attraction_force;
				g->vertices[u_idx].dy += (delta_y / distance) * attraction_force;
			}
		}
		for (int v = 0; v < g->vertex_count; v++) {
			double distance = sqrt(g->vertices[v].dx * g->vertices[v].dx + g->vertices[v].dy * g->vertices[v].dy);

			if (distance > 0.0001) {
				double displacement = min_d(distance, t);
				g->vertices[v].x += (g->vertices[v].dx / distance) * displacement;
				g->vertices[v].y += (g->vertices[v].dy / distance) * displacement;
			}

			// Ograniczenie, ¿eby wierzcho³ek nie wyszed³ poza pole (width x height)
			g->vertices[v].x = min_d(width - 10, g->vertices[v].x);
			g->vertices[v].x = (g->vertices[v].x < 10) ? 10 : g->vertices[v].x; 

			g->vertices[v].y = min_d(height - 10, g->vertices[v].y);
			g->vertices[v].y = (g->vertices[v].y < 10) ? 10 : g->vertices[v].y; 
		}

		
		t *= 0.95;
	}
}
