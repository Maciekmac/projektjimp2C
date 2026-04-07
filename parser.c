#include <stdio.h>
#include <stdlib.h>
#include "parser.h"

void init_graph(Graph *g){
    g->capacity = 10;
    g->count = 0;
    g->edges = malloc(g->capacity * sizeof(Edge));

}
int load_graph(FILE *file, Graph *g){
    char line[256];
    int line_num = 1;
    
    while(fgets(line, sizeof(line), file)){
        if(line[0] == '\n' || line[0] == '\r'){
            line_num++;
            continue;
        }
    
    if(g->count >= g->capacity){
        g->capacity *= 2;
        g->edges = realloc(g->edges, g->capacity * sizeof(Edge));
    }
    Edge *e = &g->edges[g->count];
    char garbage[2];
    int parsed = sscanf(line, "%63s %d %d %lf %1s", e->name, &e->v1, &e->v2, &e->weight, garbage);
    if (parsed != 4) {
            fprintf(stderr, "Blad formatu w pliku wejsciowym (linia %d): %s\n", line_num, line);
            return ERR_INVALID_FORMAT;
        }

        g->count++;
        line_num++;
    }
    return SUCCESS;
}
void free_graph(Graph *g) {
    if (g->edges != NULL) {
        free(g->edges);
        g->edges = NULL;
    }
    g->count = 0;
    g->capacity = 0;
}
