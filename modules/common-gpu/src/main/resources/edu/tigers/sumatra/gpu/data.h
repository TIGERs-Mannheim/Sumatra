typedef short int16_t;
typedef unsigned char uint8_t;

typedef float coord;

typedef struct _Rect {
	coord x; // top
	coord y; // left
	coord xExtend;
	coord yExtend;
} Rect;

typedef struct _Vector2 {
	coord x;
	coord y;
} Vector2;

typedef struct _Circle {
	Vector2 center;
	float radius;
} Circle;

typedef struct _Bot {
	uint8_t id;
	Vector2 pos;
} Bot;

#define fitRange(value) max(0.0f, min(1.0f, value))
