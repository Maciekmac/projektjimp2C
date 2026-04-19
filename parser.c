#include <stdio.h>
#include <stdlib.h>
#include "parser.h"

void init_graph(Graph *g){
    g->capacity = 10;
    g->count = 0;
    g->edges = malloc(g->capacity * sizeof(Edge));

    g->vertex_capacity = 10;
    g->vertex_count = 0;
    g->vertices = malloc(g->vertex_capacity * sizeof(Vertex));
}
int get_or_add_vertex(Graph* g, int id) {
    for (int i = 0; i < g->vertex_count; i++) {
        if (g->vertices[i].id == id) {
            return i;
        }
    }
    if (g->vertex_count >= g->vertex_capacity) {
        g->vertex_capacity *= 2;
        Vertex *temp = realloc(g->vertices, g->vertex_capacity * sizeof(Vertex));
        if (temp == NULL) {
            fprintf(stderr, "Blad: Brak pamieci RAM do wczytania kolejnych krawedzi!\n");
            return ERR_INVALID_FORMAT;
        }
        g->vertices = temp;
    }
    int new_index = g->vertex_count;
    g->vertices[new_index].id = id;
    g->vertices[new_index].x = 0.0;  
    g->vertices[new_index].y = 0.0;
    g->vertices[new_index].dx = 0.0;
    g->vertices[new_index].dy = 0.0;

    g->vertex_count++;

    return new_index;   
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
        Edge* temp = realloc(g->edges, g->capacity * sizeof(Edge));
        if (temp == NULL) {
            fprintf(stderr, "Blad: Brak pamieci RAM do wczytania kolejnych krawedzi!\n");
            return ERR_INVALID_FORMAT; 
        }
        g->edges = temp;
    }
    Edge *e = &g->edges[g->count];
    char smieci[2];
    int parsed = sscanf(line, "%63s %d %d %lf %1s", e->name, &e->v1, &e->v2, &e->weight, smieci);
    if (parsed != 4) {
            fprintf(stderr, "Blad formatu w pliku wejsciowym (linia %d): %s\n", line_num, line);
            return ERR_INVALID_FORMAT;
        }
        get_or_add_vertex(g, e->v1);
        get_or_add_vertex(g, e->v2);

        g->count++;
        line_num++;
    }
    return SUCCESS;
}
// Funkcja zapisująca wyliczone współrzędne do pliku
int save_graph(const char *filename, Graph *g, int binary_mode) {
    FILE *out_file;

    // TRYB BINARNY
    if (binary_mode) {
        out_file = fopen(filename, "wb");
        if (out_file == NULL) {
            fprintf(stderr, "Blad: Nie udalo sie otworzyc pliku do zapisu binarnego: %s\n", filename);
            return ERR_OUTPUT_WRITE; 
        }
        
        // Zapisujemy najpierw liczbę wierzchołków
        fwrite(&(g->vertex_count), sizeof(int), 1, out_file);
        
        // Zrzut całej tablicy wierzchołków bezpośrednio z pamięci na dysk 
        fwrite(g->vertices, sizeof(Vertex), g->vertex_count, out_file);
        
    } 
    // TRYB TEKSTOWY
    else {
        out_file = fopen(filename, "w");
        if (out_file == NULL) {
            fprintf(stderr, "Blad: Nie udalo sie otworzyc pliku do zapisu tekstowego: %s\n", filename);
            return ERR_OUTPUT_WRITE;
        }
        
        // Zapis w formacie: "ID X Y" 
        for (int i = 0; i < g->vertex_count; i++) {
            fprintf(out_file, "%d %.6f %.6f\n", 
                    g->vertices[i].id, 
                    g->vertices[i].x, 
                    g->vertices[i].y);
        }
    }

    fclose(out_file);
    printf("Zapisano wyniki do pliku: %s\n", filename);
    
    return SUCCESS;
}
void free_graph(Graph *g) {
    if (g->edges != NULL) {
        free(g->edges);
        g->edges = NULL;    
        
    }
    if (g->vertices != NULL) {
        free(g->vertices);
        g->vertices = NULL;
    }
    g->count = 0;
    g->capacity = 0;
    g->vertex_count = 0;
    g->vertex_capacity = 0;
}