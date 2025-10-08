extends Control

@export var bg_color = Color.SKY_BLUE
@export var shape_name := "linea"
@export var line_width := 60.0
@export var tracker_radius := 0
@export var line_color = Color.GRAY
@export var shape_scale := 0.9
@export var resolution := 360
@export var follow_threshold := 100.0
@export var player_selected = -1

@onready var shape_line = $MarginContainer/VBoxContainer/Control/ShapeLine
@onready var shape_area = $MarginContainer/VBoxContainer/Control/ShapeArea
@onready var tracker = $MarginContainer/VBoxContainer/Control/Tracker
@onready var tracker_shape = $MarginContainer/VBoxContainer/Control/Tracker/CollisionShape2D
@onready var collision_polygon = $MarginContainer/VBoxContainer/Control/ShapeArea/CollisionPolygon2D

@onready var menu = $MarginContainer/VBoxContainer/HBoxContainer/MenuButton
@onready var exit_menu_confirm_exit_button = $ExitButtonWindow/VBoxContainer/HBoxContainer/ConfirmExitButton
@onready var exit_menu_dont_exit_button = $ExitButtonWindow/VBoxContainer/HBoxContainer/DontExitButton

@onready var audio_player = $AudioStreamPlayer2D

var shape_points := PackedVector2Array()
var progress: Array[bool] = []

var last_frame_inside := true
var completed := false

var shapes: Array[Dictionary] = [
	{"name" : "circulo" , "level" : 1},
	{"name" : "linea", "level": 0},
	{"name" : "corazon", "level": 2},
	{"name": "diagonal", "level": 0}, 
	{"name": "triangulo", "level": 1},  
	{"name": "s", "level": 2}   
]
var shape_order = []
var ordered_shapes = []
var current_index = 0

var ready_to_process := false

func _ready():
	RenderingServer.set_default_clear_color(bg_color)
	
	$Blocker.visible = true
	$Charecter.visible = true
	$TextBallon.visible = true
	audio_player.play()
	
	await audio_player.finished
	for i in range(3, 0, -1):
		$TextBallon/Label.text = str(i)
		$TextBallon/Label.add_theme_font_size_override("font_size", 100)
		await get_tree().create_timer(1.0).timeout

	$TextBallon/Label.text = "¡Ya!"
	await get_tree().create_timer(1.0).timeout

	$Charecter.visible = false
	$TextBallon.visible = false
	$Blocker.visible = false
	
	tracker_shape.shape = CircleShape2D.new()
	tracker_radius = line_width / 2
	tracker_shape.shape.radius = tracker_radius

	# Generar lista ordenada
	for name in shape_order:
		for shape in shapes:
			if shape["name"] == name:
				ordered_shapes.append(shape)
				break
				
	menu.get_popup().add_item("Reproducir sonido", 0)
	menu.get_popup().add_item("Salir", 1)
	menu.get_popup().connect("id_pressed", self._on_menu_item_selected)
				
	exit_menu_confirm_exit_button.pressed.connect(_on_exit_menu_button_pressed.bind(true))
	exit_menu_dont_exit_button.pressed.connect(_on_exit_menu_button_pressed.bind(false))

	for shape in ordered_shapes:
		await GameData.start_game(player_selected, shape.level, shape.name)

	load_shape(ordered_shapes[current_index])
	ready_to_process = true


func _on_menu_item_selected(id):
	match id:
		0:
			audio_player.play()
		1:
			$ExitButtonWindow.show()
			$Blocker.visible = true
			
func _on_exit_menu_button_pressed(exit : bool):
	if exit:
		GameData.clear_active_games()
		GameData.clear_completed_games()
		SceneManager.go_back_to_previous_scene()
	else:
		$ExitButtonWindow.hide()
		$Blocker.visible = false


func load_shape(shape_data: Dictionary):
	var shape_name = shape_data["name"]
	var center = get_rect().size / 2
	var radius = calculate_scale()
	shape_points = generate_shape_points(shape_name, center, radius)
	draw_shape(shape_points)
	collision_polygon.polygon = shape_points
	completed = false


func _on_next_shape_pressed():
	var shape_name = ordered_shapes[current_index]["name"]

	if not completed:
		GameData.remove_active_game(shape_name)

	current_index += 1
	if current_index < ordered_shapes.size():
		load_shape(ordered_shapes[current_index])
	else:
		$EndOfShapesWindow.show()
		$Blocker.visible = true
		

func _on_end_of_game_button():
	SceneManager.go_back_to_previous_scene()


func calculate_scale() -> float:
	var size = get_rect().size
	return min(size.x, size.y) * shape_scale / 2.0

func generate_shape_points(shape: String, center: Vector2, radius: float) -> PackedVector2Array:
	var points := PackedVector2Array()

	match shape:
		"circulo":
			for i in range(resolution + 1):
				var angle = TAU * i / resolution
				points.append(center + Vector2(cos(angle), sin(angle)) * radius)
				
		"corazon":
			for i in range(resolution):
				var t = TAU * i / resolution 
				var x = 16 * pow(sin(t), 3)
				var y = 13 * cos(t) - 5 * cos(2 * t) - 2 * cos(3 * t) - cos(4 * t)
				var point = Vector2(x, -y) * (radius / 17.0)
				points.append(center + point)
				
		"linea":
			for i in range(resolution + 1):
				var t = float(i) / resolution
				points.append(center + Vector2(-radius + t * radius * 2, 0))
				
		"diagonal":
			for i in range(resolution + 1):
				var t = float(i) / resolution
				points.append(center + Vector2(-radius + t * radius * 2, -radius + t * radius * 2))
				
		"triangulo":
			var p1 = center + Vector2(0, -radius)
			var p2 = center + Vector2(radius * sin(PI / 3), radius * 0.5)
			var p3 = center + Vector2(-radius * sin(PI / 3), radius * 0.5)

			for i in range(resolution / 3):
				var t = float(i) / (resolution / 3)
				points.append(p1.lerp(p2, t))
			for i in range(resolution / 3):
				var t = float(i) / (resolution / 3)
				points.append(p2.lerp(p3, t))
			for i in range(resolution / 3 + 1):
				var t = float(i) / (resolution / 3)
				points.append(p3.lerp(p1, t))
				
		"s":
			for i in range(resolution + 1):
				var t = float(i) / resolution
				var x = lerp(-radius, radius, t)
				var y = sin(t * PI * 2) * radius * 0.5
				points.append(center + Vector2(x, y))

	return points
	
	
func draw_shape(points: PackedVector2Array):
	shape_line.clear_points()
	progress.clear()
	shape_line.width = line_width
	completed = false

	for point in points:
		shape_line.add_point(point)
		progress.append(false)

	update_line_gradient()


func _process(_delta):
	if not ready_to_process:
		return  

	var pos_in_control = get_local_mouse_position()
	var pos_global = pos_in_control + get_global_position()
	var pos_in_line2d = shape_line.to_local(pos_global)
	tracker.position = pos_in_control
	
	var is_inside = tracker.get_overlapping_areas().has(shape_area)
	
	if not is_inside and last_frame_inside:
		GameData.add_fail(ordered_shapes[current_index]["name"])
	last_frame_inside = is_inside
	
	var detection_margin = line_width * 0.5
	
	for i in range(shape_points.size()):
		if not progress[i] and pos_in_line2d.distance_to(shape_points[i]) < detection_margin:
			progress[i] = true
			
	update_line_gradient()
	check_completion()


func update_line_gradient():
	var grad := Gradient.new()
	var colors := []
	var offsets := []

	for i in range(progress.size()):
		var t = float(i) / float(progress.size() - 1)
		offsets.append(t)
		if progress[i]:
			colors.append(Color.GREEN)
		else:
			colors.append(line_color)

	grad.colors = colors
	grad.offsets = offsets
	shape_line.gradient = grad


func check_completion():
	if not completed and progress.all(func(p): return p):
		completed = true
		await get_tree().create_timer(1.0).timeout

		var shape_name = ordered_shapes[current_index]["name"]
		await GameData.end_game(shape_name, AppConstants.NAME_JUEGO_SEGUIR_LA_LINEA)

		current_index += 1
		if current_index < ordered_shapes.size():
			load_shape(ordered_shapes[current_index])
		else:
			$EndOfShapesWindow.show()
			$Blocker.visible = true
