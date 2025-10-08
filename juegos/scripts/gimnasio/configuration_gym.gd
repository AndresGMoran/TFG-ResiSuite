extends Window

var current_scene

@onready var players_menu = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/PlayersMenu
@onready var players_selected_container = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/FlowContainer
@onready var warning_label = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/FlowContainer/WarningLabel
@onready var container_verde = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/EasyContainer
@onready var container_naranja = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/MediumContainer
@onready var container_rojo = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/HardContainer
@onready var orden_ejercicios_container = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/FlowContainer2
@onready var ejercicios_warning_label = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/FlowContainer2/ExcercisesWarningLabel

var residents = []
var all_players = []
var available_players = []
var players_selected = []

var excercises = [ { "name": "Marcha", "level": 0 },
 { "name": "Brazos al lado", "level": 0 },
 { "name": "Brazos arriba sentado", "level": 0 },
 { "name": "Sentar y levantar", "level": 1 },
 { "name": "Nadar", "level": 1 },
 { "name": "Movimientos laterales", "level": 1 },
 { "name": "Moviminetos laterales + puntillas", "level": 1 },
 { "name": "Pesa arriba sentado", "level": 2 },
 { "name": "Pesa atras sentado", "level": 2 },
 { "name": "Pesas doble sentado", "level": 2 }
]
var selected_excercises_name = []
func _ready():
	while residents.is_empty():
		await get_tree().process_frame
		
	_load_residents()
	
	warning_label.visible = true
	ejercicios_warning_label.visible = true

	players_menu.get_popup().connect("id_pressed", Callable(self, "_on_player_selected"))
	create_exercise_buttons()
		
func _load_residents() -> void:
	all_players = residents.duplicate()
	available_players = all_players.duplicate()
	_update_menu()

func _update_menu():
	var popup = players_menu.get_popup()
	popup.clear()
	for player in available_players:
		var full_name = "%s %s" % [player.nombre, player.apellido]
		popup.add_item(full_name, player.id)

func _on_player_selected(id: int):
	var player = null
	for p in available_players:
		if int(p.id) == id:
			player = p
			break
	
	if player == null:
		print("No se encontró el jugador con ID: ", id)
		return

	players_selected.append(player)
	available_players.erase(player)
	_update_menu()

	var player_full_name = player.nombre + " " + player.apellido
	_add_player_visual(player_full_name, player.id)
	
	if warning_label.visible:
		warning_label.visible = false

func _add_player_visual(name: String, id: int):
	var button = Button.new()
	button.text = name + " ❌"
	button.name = str(id)  
	button.pressed.connect(Callable(self, "_remove_player").bind(id))
	players_selected_container.add_child(button)


func _remove_player(id: int):
	# Eliminar botón visual
	if players_selected_container.has_node(str(id)):
		var button = players_selected_container.get_node(str(id))
		players_selected_container.remove_child(button)
		button.queue_free()

	# Buscar jugador por ID
	for player in players_selected:
		if int(player.id) == id:
			players_selected.erase(player)
			available_players.append(player)
			break

	# Reordenar y actualizar menú
	available_players.sort_custom(func(a, b): return a.nombre < b.nombre)
	_update_menu()
	
	if players_selected.is_empty():
		warning_label.visible = true

		
func create_exercise_buttons():
	for excersice in excercises:
		var button = Button.new()
		button.text = excersice.name
		button.toggle_mode = true
		button.pressed.connect(_on_excercise_button_pressed.bind(button))
		
		button.custom_minimum_size = Vector2(160, 80)

		var style = StyleBoxFlat.new()
		var container = null

		match excersice.level:
			0:
				style.bg_color = Color(0.3, 0.8, 0.3)  # Verde
				container = container_verde
			1:
				style.bg_color = Color(1, 0.6, 0.2)    # Naranja
				container = container_naranja
			2:
				style.bg_color = Color(1, 0.3, 0.3)    # Rojo
				container = container_rojo

		button.add_theme_stylebox_override("normal", style)
		if container:
			container.add_child(button)


		
func _on_excercise_button_pressed(button):
	var name = button.text

	if button.button_pressed:
		selected_excercises_name.append(name)

		var visual = Button.new()
		visual.text = name
		visual.name = name
		visual.disabled = true
		visual.custom_minimum_size = Vector2(160, 80)

		# Obtener nivel del ejercicio
		var level = 0
		for exc in excercises:
			if exc.name == name:
				level = exc.level
				break

		visual.add_theme_color_override("font_disabled_color", Color(1, 1, 1))
		var style = StyleBoxFlat.new()
		match level:
			0:
				style.bg_color = Color(0.3, 0.8, 0.3)  # Verde
			1:
				style.bg_color = Color(1, 0.6, 0.2)    # Naranja
			2:
				style.bg_color = Color(1, 0.3, 0.3)    # Rojo

		visual.add_theme_stylebox_override("disabled", style)

		orden_ejercicios_container.add_child(visual)
		
		if ejercicios_warning_label.visible:
			ejercicios_warning_label.visible = false

	else:
		selected_excercises_name.erase(name)

		if orden_ejercicios_container.has_node(name):
			var node = orden_ejercicios_container.get_node(name)
			orden_ejercicios_container.remove_child(node)
			node.queue_free()
		
		if selected_excercises_name.is_empty():
			ejercicios_warning_label.visible = true

		
func _on_play_button_pressed():
	var error_style = StyleBoxFlat.new()
	error_style.bg_color = Color(1, 0.8, 0.8)
	var normal_style = StyleBoxFlat.new()
	normal_style.bg_color = Color("#96a7af")
	var has_error := false

	# Validar jugadores
	if players_selected.is_empty():
		warning_label.add_theme_color_override("font_color", Color(1, 0.8, 0.8))
		has_error = true
	else:
		warning_label.add_theme_color_override("font_color", Color("#96a7af"))
		
	if selected_excercises_name.is_empty():
		ejercicios_warning_label.add_theme_color_override("font_color", Color(1, 0.8, 0.8))
		has_error = true
	else:
		ejercicios_warning_label.add_theme_color_override("font_color", Color("#96a7af"))
		
	if has_error:
		return
	
	var game_scene = preload("res://scenes/gimnasio/GameGym.tscn").instantiate()
	game_scene.excercises_order = selected_excercises_name
	game_scene.players_selected = players_selected
			
	var blocker = get_parent().get_node("Blocker")
	blocker.visible = false
	self.hide()
	SceneManager.change_scene(game_scene, current_scene)

func _on_close_button_pressed() -> void:
	self.hide()
	var blocker = get_parent().get_node("Blocker")
	blocker.visible = false
	
func default_config():
	players_selected.clear()
	selected_excercises_name.clear()
	_load_residents() 

	for child in players_selected_container.get_children():
		if child is Button:
			players_selected_container.remove_child(child)
			child.queue_free()

	# Limpiar botones visuales de ejercicios seleccionados
	for child in orden_ejercicios_container.get_children():
		if child is Button:
			orden_ejercicios_container.remove_child(child)
			child.queue_free()

	warning_label.visible = true
	ejercicios_warning_label.visible = true
	warning_label.add_theme_color_override("font_color", Color("#96a7af"))
	ejercicios_warning_label.add_theme_color_override("font_color", Color("#96a7af"))


	for container in [container_verde, container_naranja, container_rojo]:
		for button in container.get_children():
			if button is Button:
				button.button_pressed = false
