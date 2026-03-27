#ifndef PARSER_H
#define PARSER_H

#include <stdio.h>
#include "config.h" 

#define MAX_EDGE_NAME 64

// Struktura pojedynczej krawędzi
typedef struct {
    char name[MAX_EDGE_NAME];
    int v1;
    int v2;
    double weight;
} Edge;

// Struktura przechowująca wszystkie wczytane krawędzie
typedef struct {
    Edge *edges;      
    int count;        
    int capacity;     
} Graph;


void init_graph(Graph *g);
int load_graph(FILE *file, Graph *g);
void free_graph(Graph *g);

#endif 
