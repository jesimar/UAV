/**
* Author: Jesimar da Silva Arantes
* Date: 08/06/2016
* Last Update: 21/09/2018
* Description: Program to generate routes with standard formats for the UAV.
* Descrição: Programa para gerar rotas com formatos padrões para o VANT.
*/

//============================USED LIBRARIES============================

#include <stdio.h>
#include <math.h>
#include <string.h>
#include <stdlib.h>

#define TRUE 1
#define FALSE 0

#define CIRC_MERIDIONAL 40007860.0
#define CIRC_EQUATORIAL 40075017.0

//===========================GLOBAL VARIABLES===========================

int IS_PRINT = FALSE;

double radius_circle;
double base_triangle;
double base_rectangle;

double lat;
double lng;
double alt;

//=========================STRUCTS and TYPEDEF==========================

typedef struct pos{
	double posX;
	double posY;
	double posZ;
}position;

//=========================FUNCTION PROTOTYPES==========================

void createRouteCircleGeo(int discretization, char name[]);
void createRouteTriangleGeo(int discretization, char name[]);
void createRouteRectangleGeo(int discretization, char name[]);

double convertYtoLatitude(double latBase, double y);
double convertXtoLongitude(double lonBase, double latBase, double x);

//==============================FUNCTIONS===============================

//./uav-routes-standard lat lng alt type dist discretization
int main(int argc, char **argv){
	lat = atof(argv[1]);
	lng = atof(argv[2]);
	alt = atof(argv[3]);
	double dist = atof(argv[5]);
	int discretization = atoi(argv[6]);
	
	char str_circle[10];
	strcpy(str_circle, "CIRCLE");
	
	char str_triangle[10];
	strcpy(str_triangle, "TRIANGLE");
	
	char str_rectangle[11];
	strcpy(str_rectangle, "RECTANGLE");
	
	if (strcmp(argv[4], str_circle) == 0){
		radius_circle = dist;
		createRouteCircleGeo(discretization, "route-behavior.txt");
		printf("behavior generated by circle\n");
	}else if (strcmp(argv[4], str_triangle) == 0){
		base_triangle = dist;
		createRouteTriangleGeo(discretization, "route-behavior.txt");
		printf("behavior generated by triangle\n");
	}else if (strcmp(argv[4], str_rectangle) == 0){
		base_rectangle = dist;
		createRouteRectangleGeo(discretization, "route-behavior.txt");
		printf("behavior generated by rectangle\n");
	}else{
		printf("non-generated behavior\n");
	}
	return 0;
}

/**
 * Função usada para criar uma rota em formato circular em coordenadas geográficas.
 */
void createRouteCircleGeo(int discretization, char name[]){
	//printf("\n------------%s------------\n\n", name);
	FILE *file = fopen(name, "w+");
	position pos;
	pos.posX = 0;
	pos.posY = 0;
	pos.posZ = alt;
	double radius = radius_circle;
	double a = radius_circle;
	double b = 0.0;
	double step_angle = M_PI * (360.0/discretization) /180.0;
	for (int i = 0; i < discretization+1; i++){
		double angle = M_PI - step_angle * i;
		pos.posX = a + radius*cos(angle);
		pos.posY = b + radius*sin(angle);
		pos.posX = convertXtoLongitude(lng, lat, pos.posX);
		pos.posY = convertYtoLatitude(lat, pos.posY);
		if (IS_PRINT == TRUE){
			printf("%2.8f %2.8f %2.2f\n", pos.posY, pos.posX, pos.posZ);
		}
		fprintf(file, "%2.8f;%2.8f;%2.2f\n", pos.posY, pos.posX, pos.posZ);
	}
	fclose(file);
}

/**
 * Função usada para criar uma rota em formato triangular em coordenadas geográficas.
 */
void createRouteTriangleGeo(int discretization, char name[]){
	//printf("\n------------%s------------\n\n", name);
	FILE *file = fopen(name, "w+");
	position pos;
	position posP;
	pos.posX = 0;
	pos.posY = 0;
	posP.posX = convertXtoLongitude(lng, lat, pos.posX);
	posP.posY = convertYtoLatitude(lat, pos.posY);
	posP.posZ = alt;
	double inc = 3.0*base_triangle/discretization;
	for (int i = 0; i < 3; i++){
		for (int j = 0; j < discretization/3; j++){
			if (IS_PRINT == TRUE){
				printf("%2.8f %2.8f %2.2f\n", posP.posY, posP.posX, posP.posZ);
			}
			fprintf(file, "%2.8f;%2.8f;%2.2f\n", posP.posY, posP.posX, posP.posZ);
			if (i == 0){
				pos.posY = pos.posY + inc;
			}else if (i == 1){
				pos.posX = pos.posX + sqrt(3.0) * inc * cos(M_PI/3.0);
				pos.posY = pos.posY - inc * sin(M_PI/6.0);
			}else if (i == 2){
				pos.posX = pos.posX - sqrt(3.0) * inc * cos(M_PI/3.0);
				pos.posY = pos.posY - inc * sin(M_PI/6.0);
			}
			posP.posX = convertXtoLongitude(lng, lat, pos.posX);
			posP.posY = convertYtoLatitude(lat, pos.posY);
		}
	}
	pos.posX = 0;
	pos.posY = 0;
	posP.posX = convertXtoLongitude(lng, lat, pos.posX);
	posP.posY = convertYtoLatitude(lat, pos.posY);
	if (IS_PRINT == TRUE){
		printf("%2.8f %2.8f %2.2f\n", posP.posY, posP.posX, posP.posZ);
	}
	fprintf(file, "%2.8f;%2.8f;%2.2f\n", posP.posY, posP.posX, posP.posZ);
	fclose(file);
}

/**
 * Função usada para criar uma rota em formato retangular (quadrado) em coordenadas geográficas.
 */
void createRouteRectangleGeo(int discretization, char name[]){
	//printf("\n------------%s------------\n\n", name);
	FILE *file = fopen(name, "w+");
	position pos;
	position posP;
	pos.posX = 0;
	pos.posY = 0;
	posP.posX = convertXtoLongitude(lng, lat, pos.posX);
	posP.posY = convertYtoLatitude(lat, pos.posY);
	posP.posZ = alt;
	double inc = 4.0*base_rectangle/discretization;
	for (int i = 0; i < 4; i++){
		for (int j = 0; j < discretization/4; j++){
			if (IS_PRINT == TRUE){
				printf("%2.8f %2.8f %2.2f\n", posP.posY, posP.posX, posP.posZ);
			}
			fprintf(file, "%2.8f;%2.8f;%2.2f\n", posP.posY, posP.posX, posP.posZ);
			if (i == 0){
				pos.posY = pos.posY + inc;
			}else if (i == 1){
				pos.posX = pos.posX + inc;
			}else if (i == 2){
				pos.posY = pos.posY - inc;
			}else if (i == 3){
				pos.posX = pos.posX - inc;
			}
			posP.posX = convertXtoLongitude(lng, lat, pos.posX);
			posP.posY = convertYtoLatitude(lat, pos.posY);
		}
	}
	pos.posX = 0;
	pos.posY = 0;
	posP.posX = convertXtoLongitude(lng, lat, pos.posX);
	posP.posY = convertYtoLatitude(lat, pos.posY);
	posP.posZ = alt;
	if (IS_PRINT == TRUE){
		printf("%2.8f %2.8f %2.2f\n", posP.posY, posP.posX, posP.posZ);
	}
	fprintf(file, "%2.8f;%2.8f;%2.2f\n", posP.posY, posP.posX, posP.posZ);
	fclose(file);
}

double convertYtoLatitude(double latBase, double y){
	return latBase + y * 360/CIRC_MERIDIONAL;
}

double convertXtoLongitude(double lonBase, double latBase, double x){
	return lonBase + x * 360/(CIRC_EQUATORIAL * cos(latBase * M_PI/180));
}
