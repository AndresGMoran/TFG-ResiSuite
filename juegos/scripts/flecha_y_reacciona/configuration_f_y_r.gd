extends Window

var current_scene


@onready var menu_button: MenuButton = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/MenuButton
@onready var seleccionados_container: FlowContainer = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/FlowContainer
@onready var warning_label = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/FlowContainer/WarningLabel
@onready var seconds_input: LineEdit = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/Seconds

@onready var button_2_directions = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/HBoxContainer/Button2Directions
@onready var button_4_directions = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/HBoxContainer/Button4Directions
@onready var button_8_directions = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/HBoxContainer/Button8Directions

@onready var repeats_per_direction_input = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/RepeatsPerDirection

@onready var bg_color_picker: ColorPickerButton = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/BackgroundColorPickerButton
@onready var arrow_color_picker: ColorPickerButton = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/ArrowColorPickerButton

var residents = []

var todos_los_jugadores = []
var jugadores_disponibles = []
var jugadores_seleccionados = []

var directions = 0
var level = -1

func _ready():
	while residents.is_empty():
		await get_tree().process_frame
		
	_load_residents()
	
	warning_label.visible = true
	
	# Conectar señal de selección
	menu_button.get_popup().connect("id_pressed", Callable(self, "_on_jugador_seleccionado"))
	
	button_2_directions.pressed.connect(Callable(self, "_on_directions_pressed").bind(button_2_directions))
	button_4_directions.pressed.connect(Callable(self, "_on_directions_pressed").bind(button_4_directions))
	button_8_directions.pressed.connect(Callable(self, "_on_directions_pressed").bind(button_8_directions))

func _load_residents() -> void:
	todos_los_jugadores = residents.duplicate()
	jugadores_disponibles = todos_los_jugadores.duplicate()
	_actualizar_menu()

func _actualizar_menu():
	var popup = menu_button.get_popup()
	popup.clear()
	for jugador in jugadores_disponibles:
		var full_name = jugador.nombre + " " + jugador.apellido
		popup.add_item(full_name, jugador.id)
		

func _on_jugador_seleccionado(id: int):
	var jugador = null
	for j in jugadores_disponibles:
		if int(j.id) == id:
			jugador = j
			break
	
	if jugador == null:
		print("No se encontró el jugador con ID: ", id)
		return

	jugadores_seleccionados.append(jugador)
	jugadores_disponibles.erase(jugador)
	_actualizar_menu()

	var jugador_full_name = jugador.nombre + " " + jugador.apellido
	_agregar_jugador_visual(jugador_full_name)

	if warning_label.visible:
		warning_label.visible = false


func _agregar_jugador_visual(nombre: String):
	var boton = Button.new()
	boton.text = nombre + " ❌"
	boton.name = nombre
	boton.pressed.connect(Callable(self, "_quitar_jugador").bind(nombre))
	seleccionados_container.add_child(boton)

func _quitar_jugador(nombre: String):
	# Quitar de la vista
	var boton = seleccionados_container.get_node(nombre)
	if boton:
		seleccionados_container.remove_child(boton)
		boton.queue_free()

	# Buscar el jugador por nombre
	for jugador in jugadores_seleccionados:
		if jugador.nombre == nombre.split(" ")[0]:
			jugadores_seleccionados.erase(jugador)
			jugadores_disponibles.append(jugador)
			break

	jugadores_disponibles.sort_custom(func(a, b): return a.nombre < b.nombre)
	_actualizar_menu()

	if jugadores_seleccionados.is_empty():
		warning_label.visible = true

		
func _on_directions_pressed(button : Button):
	# Desmarcar todos los botones
	button_2_directions.button_pressed = false
	button_4_directions.button_pressed = false
	button_8_directions.button_pressed = false

	# Marcar el botón seleccionado
	button.button_pressed = true

	# Actualizar el valor de directions
	match button:
		button_2_directions:
			directions = 2
			level = 0
		button_4_directions:
			directions = 4
			level = 1
		button_8_directions:
			directions = 8
			level = 2
			
func reset_configuracion():
	jugadores_seleccionados.clear()
	jugadores_disponibles = todos_los_jugadores.duplicate()
	_actualizar_menu()

	# Limpiar visualización de jugadores seleccionados
	for child in seleccionados_container.get_children():
		seleccionados_container.remove_child(child)
		child.queue_free()
	
	# Resetear valores de configuración
	seconds_input.clear()
	repeats_per_direction_input.clear()
	warning_label.visible = false
	
	button_2_directions.button_pressed = false
	button_4_directions.button_pressed = false
	button_8_directions.button_pressed = false
	directions = 0
	level = -1
	
func _on_play_button_pressed():
	var error_style = StyleBoxFlat.new()
	error_style.bg_color = Color(1, 0.8, 0.8)
	var normal_style = StyleBoxFlat.new()
	normal_style.bg_color = Color("#96a7af")
	var has_error := false

	# Validar jugadores
	if jugadores_seleccionados.is_empty():
		warning_label.add_theme_color_override("font_color", Color(1, 0.8, 0.8))
		has_error = true
	else:
		warning_label.add_theme_color_override("font_color", Color("#96a7af"))

	# Validar segundos
	if seconds_input.text.is_empty():
		seconds_input.add_theme_stylebox_override("normal", error_style)
		has_error = true
	else:
		seconds_input.add_theme_stylebox_override("normal", normal_style)

	# Validar repeticiones
	if repeats_per_direction_input.text.is_empty():
		repeats_per_direction_input.add_theme_stylebox_override("normal", error_style)
		has_error = true
	else:
		repeats_per_direction_input.add_theme_stylebox_override("normal", normal_style)

	# Validar dirección
	if directions == 0:
		button_2_directions.add_theme_stylebox_override("normal", error_style)
		button_4_directions.add_theme_stylebox_override("normal", error_style)
		button_8_directions.add_theme_stylebox_override("normal", error_style)
		has_error = true
	else:
		button_2_directions.add_theme_stylebox_override("normal", normal_style)
		button_4_directions.add_theme_stylebox_override("normal", normal_style)
		button_8_directions.add_theme_stylebox_override("normal", normal_style)

	if has_error:
		return
		
	var game_scene = preload("res://scenes/flecha_y_reacciona/GameFlechaYReacciona.tscn").instantiate()
	
	game_scene.bg_color = bg_color_picker.color
	game_scene.arrow_color = arrow_color_picker.color
	
	game_scene.interval = int(seconds_input.text)
	game_scene.direction_count = directions
	game_scene.max_repeats_per_direction = repeats_per_direction_input.text
	game_scene.selected_players = jugadores_seleccionados
	game_scene.level = level
	
	var blocker = get_parent().get_node("Blocker")
	blocker.visible = false
	self.hide()
	SceneManager.change_scene(game_scene, current_scene)


func _on_close_button_pressed() -> void:
	self.hide()
	var blocker = get_parent().get_node("Blocker")
	blocker.visible = false
	
func default_config():
	jugadores_seleccionados.clear()
	_load_residents()
	
	for child in seleccionados_container.get_children():
		if child is Button:
			seleccionados_container.remove_child(child)
			child.queue_free()
			
	warning_label.add_theme_color_override("font_color", Color("#96a7af"))

	# Resetear campos de texto
	seconds_input.clear()
	repeats_per_direction_input.clear()

	# Resetear botones de dirección
	button_2_directions.button_pressed = false
	button_4_directions.button_pressed = false
	button_8_directions.button_pressed = false
	directions = 0
	level = -1

	var normal_style = StyleBoxFlat.new()
	normal_style.bg_color = Color("#96a7af")

	seconds_input.add_theme_stylebox_override("normal", normal_style)
	repeats_per_direction_input.add_theme_stylebox_override("normal", normal_style)

	button_2_directions.add_theme_stylebox_override("normal", normal_style)
	button_4_directions.add_theme_stylebox_override("normal", normal_style)
	button_8_directions.add_theme_stylebox_override("normal", normal_style)

	
	bg_color_picker.color = Color("#ffffff") 
	arrow_color_picker.color = Color("b30017")
