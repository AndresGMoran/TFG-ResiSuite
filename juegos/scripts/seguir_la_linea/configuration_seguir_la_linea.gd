extends Window

const OFFLINE_MODE := false  # Cambiar a true para pruebas sin API


@onready var players_menu = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/PlayersMenu
@onready var container_verde = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/EasyContainer
@onready var container_naranja = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/MediumContainer
@onready var container_rojo = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/HardContainer
@onready var orden_de_formas_container = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/FlowContainer
@onready var shapes_warning_label = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/FlowContainer/ShapesWarningLabel
@onready var hslider1 = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/size_slider
@onready var hslider2 = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/width_slider

var current_scene
var residents = []
var all_players = []
var player_selected = -1

var shapes = [
	{"name" : "circulo" , "level" : 1},
	{"name" : "linea", "level" : 0},
	{"name" : "corazon", "level" : 2},
	{"name": "diagonal", "level": 0}, 
	{"name": "triangulo", "level": 1},  
	{"name": "s", "level": 2}  

]
var selected_shapes_name = []

func _ready():
	while residents.is_empty():
		await get_tree().process_frame
	
	_load_residents()
	
	shapes_warning_label.visible = true

	var slider_style = StyleBoxFlat.new()
	slider_style.content_margin_top = 8
	slider_style.content_margin_bottom = 8

	var grabber_style = StyleBoxFlat.new()
	grabber_style.content_margin_left = 10
	grabber_style.content_margin_right = 10
	grabber_style.content_margin_top = 10
	grabber_style.content_margin_bottom = 10

	hslider1.add_theme_stylebox_override("slider", slider_style)
	hslider1.add_theme_stylebox_override("grabber", grabber_style)

	hslider2.add_theme_stylebox_override("slider", slider_style)
	hslider2.add_theme_stylebox_override("grabber", grabber_style)

	players_menu.get_popup().connect("id_pressed", Callable(self, "_on_player_selected"))
	
		
func _load_residents() -> void:
	var popup = players_menu.get_popup()
	popup.clear()

	for resident in residents:
		var full_name = "%s %s" % [resident.nombre, resident.apellido]
		var id = int(resident.id)
		popup.add_item(full_name, id)

	for resident in all_players:
		var full_name = "%s %s" % [resident.nombre, resident.apellido]
		var id = int(resident.id)
		popup.add_item(full_name, id)

func _on_player_selected(id):
	player_selected = id
	for resident in residents:
		if resident.id == id:
			var full_name = "%s %s" % [resident.nombre, resident.apellido]
			players_menu.text = full_name

func create_shapes_buttons():
	for shape in shapes:
		var button = Button.new()
		button.text = shape.name
		button.toggle_mode = true
		button.pressed.connect(_on_shape_button_pressed.bind(button))
		button.custom_minimum_size = Vector2(160, 80)

		var style = StyleBoxFlat.new()
		var container = null

		match shape.level:
			0:
				style.bg_color = Color(0.3, 0.8, 0.3)
				container = container_verde
			1:
				style.bg_color = Color(1, 0.6, 0.2)
				container = container_naranja
			2:
				style.bg_color = Color(1, 0.3, 0.3)
				container = container_rojo

		button.add_theme_stylebox_override("normal", style)
		button.button_pressed = false
		if container:
			container.add_child(button)

func _on_shape_button_pressed(button):
	var name = button.text

	if button.button_pressed:
		selected_shapes_name.append(name)

		var visual = Button.new()
		visual.text = name
		visual.name = name
		visual.disabled = true
		visual.custom_minimum_size = Vector2(160, 80)

		var level = 0
		for shape in shapes:
			if shape.name == name:
				level = shape.level
				break

		visual.add_theme_color_override("font_disabled_color", Color(1, 1, 1))
		var style = StyleBoxFlat.new()
		match level:
			0:
				style.bg_color = Color(0.3, 0.8, 0.3)
			1:
				style.bg_color = Color(1, 0.6, 0.2)
			2:
				style.bg_color = Color(1, 0.3, 0.3)

		visual.add_theme_stylebox_override("disabled", style)
		orden_de_formas_container.add_child(visual)
		
		if shapes_warning_label.visible:
			shapes_warning_label.visible = false

	else:
		selected_shapes_name.erase(name)

		if orden_de_formas_container.has_node(name):
			var node = orden_de_formas_container.get_node(name)
			orden_de_formas_container.remove_child(node)
			node.queue_free()
		if selected_shapes_name.is_empty():
			shapes_warning_label.visible = true

func _on_play_button_pressed():
	var bg_color = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/bg_color_picker.color
	var line_color = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/line_color_picker.color
	var radius = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/size_slider.value
	var width = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/width_slider.value

	var error_style = StyleBoxFlat.new()
	error_style.bg_color = Color(1, 0.8, 0.8)
	var normal_style = StyleBoxFlat.new()
	normal_style.bg_color = Color("#96a7af")
	var has_error := false
	
	if player_selected == -1:
		players_menu.add_theme_stylebox_override("normal", error_style)
		has_error =  true
	else:
		players_menu.add_theme_stylebox_override("normal", normal_style)

	# Validar formas seleccionadas
	if selected_shapes_name.is_empty():
		shapes_warning_label.add_theme_color_override("font_color", Color(1, 0.8, 0.8))
		has_error = true
	else:
		shapes_warning_label.add_theme_color_override("font_color", Color("#96a7af"))
		
	if has_error:
		return

	var game_scene = preload("res://scenes/seguir_la_linea/GameSeguirLaLinea.tscn").instantiate()
				
	game_scene.line_width = width
	game_scene.shape_scale = radius
	game_scene.line_color = line_color
	game_scene.bg_color = bg_color
	game_scene.shape_order = selected_shapes_name
	game_scene.player_selected = player_selected

	var blocker = get_parent().get_node("Blocker")
	blocker.visible = false
	self.hide()
	
	SceneManager.change_scene(game_scene, current_scene)
	
func _on_close_button_pressed() -> void:
	self.hide()
	var blocker = get_parent().get_node("Blocker")
	blocker.visible = false
	
func default_config():
	var normal_style = StyleBoxFlat.new()
	normal_style.bg_color = Color("#96a7af")
	players_menu.add_theme_stylebox_override("normal", normal_style)
	players_menu.text = "Seleccionar jugador"
	player_selected = -1
	
	shapes_warning_label.add_theme_color_override("font_color", Color("#96a7af"))
	
	for child in orden_de_formas_container.get_children():
		if child is Button:
			child.queue_free()
	
	for child in container_verde.get_children():
		if child is Button:
			child.queue_free()
	
	for child in container_naranja.get_children():
		if child is Button:
			child.queue_free()
			
	for child in container_rojo.get_children():
		if child is Button:
			child.queue_free()
			
	create_shapes_buttons()
	selected_shapes_name = []
	
	
	
