extends Window

var current_scene

@onready var players_menu = $MarginContainer/VBoxContainer/MenuButton
@onready var seleccionados_container = $MarginContainer/VBoxContainer/FlowContainer
@onready var warning_label = $MarginContainer/VBoxContainer/FlowContainer/WarningLabel
@onready var play_button = $MarginContainer/VBoxContainer/PlayButton

var residents = []
var todos_los_jugadores = [] 
var jugadores_disponibles = []
var jugadores_seleccionados = []

func _ready():
	while residents.is_empty():
		await get_tree().process_frame
		
	_load_residents()
	
	warning_label.visible = true
	players_menu.get_popup().connect("id_pressed", Callable(self, "_on_jugador_seleccionado"))
		
func _load_residents() -> void:
	todos_los_jugadores = residents.duplicate()
	jugadores_disponibles = todos_los_jugadores.duplicate()
	_actualizar_menu()

func _actualizar_menu():
	var popup = players_menu.get_popup()
	popup.clear()
	for player in jugadores_disponibles:
		var full_name = "%s %s" % [player.nombre, player.apellido]
		popup.add_item(full_name, player.id)

func _on_jugador_seleccionado(id: int):
	var player = null
	for p in jugadores_disponibles:
		if int(p.id) == id:
			player = p
			break
	
	if player == null:
		print("No se encontró el jugador con ID: ", id)
		return

	jugadores_seleccionados.append(player)
	jugadores_disponibles.erase(player)
	_actualizar_menu()

	var player_full_name = player.nombre + " " + player.apellido
	_agregar_jugador_visual(player_full_name, player.id)
	
	if warning_label.visible:
		warning_label.visible = false
	

func _agregar_jugador_visual(nombre: String, id: int):
	var boton = Button.new()
	boton.text = nombre + " ❌"
	boton.name = str(id)
	boton.pressed.connect(Callable(self, "_quitar_jugador").bind(id))
	seleccionados_container.add_child(boton)

func _quitar_jugador(id: int):
	# Eliminar botón visual de seleccionados
	print(seleccionados_container.get_node(str(id)))
	if seleccionados_container.has_node(str(id)):
		var button = seleccionados_container.get_node(str(id))
		seleccionados_container.remove_child(button)
		button.queue_free()

	# Mover jugador de seleccionados a disponibles
	for player in jugadores_seleccionados:
		if int(player.id) == id:
			jugadores_seleccionados.erase(player)
			jugadores_disponibles.append(player)
			break

	# Reordenar y actualizar menú
	jugadores_disponibles.sort_custom(func(a, b): return a.nombre < b.nombre)
	_actualizar_menu()
	
	if jugadores_seleccionados.is_empty():
		warning_label.visible = true

	
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
		
	if has_error:
		return
	
	
	var game_scene = preload("res://scenes/bingo_auditivo/GameBingo.tscn").instantiate()
	game_scene.jugadores_seleccionados = jugadores_seleccionados
		
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
