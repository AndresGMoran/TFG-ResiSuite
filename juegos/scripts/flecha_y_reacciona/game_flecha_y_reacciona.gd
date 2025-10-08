extends Control

@export var bg_color: Color = Color(1, 0, 0)
@export var arrow_color: Color = Color(1, 0, 0)
@export var direction_count = -1
@export var interval = 2
@export var margin_ratio = 0.1
@export var max_repeats_per_direction: int = 3

@export var selected_players = []
@export var level = -1

var ganador_index = -1
var ganador = null

@onready var winner_selector = $WinnerSelector
@onready var menu_players = $WinnerSelector/VBoxContainer/MenuPlayers
@onready var audio_player = $AudioStreamPlayer2D

@onready var exit_menu_confirm_exit_button = $ExitButtonWindow/VBoxContainer/HBoxContainer/ConfirmExitButton
@onready var exit_menu_cancel_exit_button = $ExitButtonWindow/VBoxContainer/HBoxContainer/CancelExitButton

var directions: Array[int] = []
var direction_usage: Dictionary = {}
var arrow: Polygon2D
var last_angle: int = -1
var direction_timer: Timer
var end_popup: Popup

func _ready():
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
	
	RenderingServer.set_default_clear_color(bg_color)
	
	for jugador in selected_players:
		GameData.start_game(jugador.id, level, "")
	
	get_viewport().connect("size_changed", Callable(self, "_on_viewport_resized"))
	
	$VBoxContainer/MenuButton.get_popup().add_item("Reproducir sonido", 0)
	$VBoxContainer/MenuButton.get_popup().add_item("Salir", 1)
	$VBoxContainer/MenuButton.get_popup().connect("id_pressed", self._on_menu_item_selected)
	
	exit_menu_confirm_exit_button.pressed.connect(_on_exit_menu_button_pressed.bind(true))
	exit_menu_cancel_exit_button.pressed.connect(_on_exit_menu_button_pressed.bind(false))
	
	# Crear flecha
	arrow = Polygon2D.new()
	$VBoxContainer.add_child(arrow)
	arrow.polygon = _generate_arrow_shape()
	arrow.color = arrow_color
	_update_arrow_transform()

	# Configurar posibles direcciones
	_set_possible_directions()
	change_direction()

	# Crear y configurar Timer
	direction_timer = Timer.new()
	direction_timer.wait_time = interval
	direction_timer.autostart = true
	direction_timer.one_shot = false
	direction_timer.connect("timeout", Callable(self, "change_direction"))
	add_child(direction_timer)
	direction_timer.start()

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
		SceneManager.go_back_to_previous_scene()
	else:
		$ExitButtonWindow.hide()
		$Blocker.visible = false


func _generate_arrow_shape() -> PackedVector2Array:
	return [
		Vector2(0, -1),
		Vector2(0.4, 0.4),
		Vector2(0.2, 0.4),
		Vector2(0.2, 1),
		Vector2(-0.2, 1),
		Vector2(-0.2, 0.4),
		Vector2(-0.4, 0.4),
	]

func _update_arrow_transform():
	var screen_size = get_viewport().get_visible_rect().size
	var min_dim = min(screen_size.x, screen_size.y)
	var usable_space = min_dim * (1.0 - margin_ratio * 2)

	arrow.scale = Vector2(usable_space / 2, usable_space / 2)
	arrow.position = screen_size / 2

func _on_viewport_resized():
	_update_arrow_transform()

func _set_possible_directions():
	match direction_count:
		2:
			directions = [0, 180]
		4:
			directions = [0, 90, 180, 270]
		8:
			directions = [0, 45, 90, 135, 180, 225, 270, 315]

	direction_usage.clear()
	for dir in directions:
		direction_usage[dir] = 0

func change_direction():
	var possible = directions.filter(func(d):
		return direction_usage[d] < max_repeats_per_direction
	)

	if last_angle != -1 and possible.size() > 1:
		possible = possible.filter(func(d):
			return d != last_angle
		)

	if possible.is_empty():
		direction_timer.stop()
		end_of_game()
		return

	var angle = possible[randi() % possible.size()]
	last_angle = angle
	direction_usage[angle] += 1
	arrow.rotation_degrees = angle

func end_of_game():
	for i in selected_players.size():
		var full_name = "%s %s" % [selected_players[i].nombre, selected_players[i].apellido]
		menu_players.get_popup().add_item(full_name, i)
	menu_players.get_popup().connect("id_pressed", Callable(self, "_on_ganador_seleccionado"))
	winner_selector.show()
	$Blocker.visible = true
	
func _on_ganador_seleccionado(index: int):
	if ganador_index != -1:
		GameData.remove_victory_by_resident(selected_players[ganador_index].id)

	ganador_index = index
	ganador = selected_players[index]
	GameData.add_victory_by_resident(ganador.id)

	var full_name = "%s %s" % [ganador.nombre, ganador.apellido]
	menu_players.text = full_name
	
func _on_winner_button_pressed():
	GameData.end_game("", AppConstants.NAME_JUEGO_FLECHA_Y_REACCIONA)
	SceneManager.go_back_to_previous_scene()
