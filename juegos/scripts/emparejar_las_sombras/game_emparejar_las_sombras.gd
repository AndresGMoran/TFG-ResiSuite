extends Control

@onready var game_board_container = $VBoxContainer/MarginContainer/HBoxContainer/VBoxContainer2/GameBoardContainer
@onready var game_board = $VBoxContainer/MarginContainer/HBoxContainer/VBoxContainer2/GameBoardContainer/GameBoard
@onready var tiles_container = $VBoxContainer/MarginContainer/HBoxContainer/VBoxContainer/TilesContainer
@onready var tiles_label = $VBoxContainer/MarginContainer/HBoxContainer/VBoxContainer/TilesLabel
@onready var board_label = $VBoxContainer/MarginContainer/HBoxContainer/VBoxContainer2/BoardLabel
@onready var audio_player = $AudioStreamPlayer

@onready var exit_menu_confirm_exit_button = $ExitButtonWindow/VBoxContainer/HBoxContainer/ConfirmExitButton
@onready var exit_menu_cancel_exit_button = $ExitButtonWindow/VBoxContainer/HBoxContainer/CancelExitButton


@export var bg_color: Color = Color(1, 0, 0)
@export var table_lines_color: Color = Color(1, 0, 0)
@export var tiles_lines_color: Color = Color(1, 0, 0)

@export var player_selected = -1
@export var difficulty = -1 # 0: Fácil, 1: Medio, 2: Difícil

var board_size = Vector2i(5, 5)
var game_numbers = []
var number_tiles = []
var used_animals = []

func _ready():
	RenderingServer.set_default_clear_color(bg_color)
	await get_tree().process_frame
	set_difficulty()
	
	$Blocker.visible = true
	set_tiles_interaction(false)
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
	set_tiles_interaction(true)
	
	GameData.start_game(player_selected, difficulty, "")
	
	$VBoxContainer/MenuButton.get_popup().add_item("Reproducir sonido", 0)
	$VBoxContainer/MenuButton.get_popup().add_item("Salir", 1)
	$VBoxContainer/MenuButton.get_popup().connect("id_pressed", self._on_menu_item_selected)
	
	exit_menu_confirm_exit_button.pressed.connect(_on_exit_menu_button_pressed.bind(true))
	exit_menu_cancel_exit_button.pressed.connect(_on_exit_menu_button_pressed.bind(false))

func set_difficulty():
	match difficulty:
		0:
			board_size = Vector2i(3, 3)
		1:
			board_size = Vector2i(4, 4)
		2:
			board_size = Vector2i(5, 5)
	setup_game()

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

func setup_game():
	used_animals.clear()
	number_tiles.clear()
	game_board.get_node("GridContainer").columns = board_size.x
	var grid = game_board.get_node("GridContainer")
	for child in grid.get_children():
		grid.remove_child(child)
		child.queue_free()

	for child in tiles_container.get_children():
		tiles_container.remove_child(child)
		child.queue_free()


	game_numbers = generate_unique_numbers(board_size.x * board_size.y)
	game_numbers.shuffle()

	create_board_labels()
	create_number_tiles()
	
	var cell_size = get_responsive_cell_size()
	game_board_container.size = Vector2(
		cell_size.x * board_size.x,
		cell_size.y * board_size.y 
	)
	
	game_board.size = Vector2(
		cell_size.x * board_size.x,
		cell_size.y * board_size.y 
	)

	game_board.size_flags_vertical = Control.SIZE_EXPAND_FILL


func generate_unique_numbers(count: int) -> Array[int]:
	var numbers: Array[int] = []
	for i in range(1, count + 1):
		numbers.append(i)
	return numbers

func get_responsive_cell_size() -> Vector2:
	var container_size = game_board_container.size

	var total_x = board_size.x
	var total_y = board_size.y

	var w = container_size.x / total_x
	var h = container_size.y / total_y
	return Vector2(w, h).clamp(Vector2(64, 64), Vector2(256, 256))
	

func create_board_labels():
	board_label.add_theme_color_override("font_color", table_lines_color)
	
	var animal_images = ImageLoader.get_all_animal_shadow_images()
	animal_images.shuffle()
	
	var board_slots_count = game_numbers.size()
	var fixed_cell_size = get_responsive_cell_size()
	for i in range(board_slots_count):
		var texture = animal_images[i % animal_images.size()]
		var sprite = Sprite2D.new()
		sprite.name = "Sprite2D"
		sprite.texture = texture
		sprite.scale = Vector2(fixed_cell_size.x / sprite.texture.get_width(), fixed_cell_size.y / sprite.texture.get_height())
		sprite.position = fixed_cell_size / 2
		var cell = Node2D.new()
		cell.position = Vector2(i % board_size.x * fixed_cell_size.x, int(i / board_size.x) * fixed_cell_size.y)
		
		var border = Line2D.new()
		var margin = 5.0
		border.add_point(Vector2(margin, margin))
		border.add_point(Vector2(fixed_cell_size.x - margin, margin))
		border.add_point(Vector2(fixed_cell_size.x - margin, fixed_cell_size.y - margin))
		border.add_point(Vector2(margin, fixed_cell_size.y - margin))
		border.add_point(Vector2(margin, margin)) # cerrar la forma
		border.width = 4
		border.default_color = table_lines_color
		border.antialiased = false
		cell.add_child(border)
		
		cell.add_child(sprite)
		var image_path = texture.resource_path
		var animal_name = image_path.get_file().replace("_sombra.png", "")

		used_animals.append(animal_name)
		cell.set("target_animal", animal_name)
		cell.set("is_target", true)
		cell.set("is_occupied", false)
		cell.name = animal_name
		game_board.get_node("GridContainer").add_child(cell)

		var static_body = StaticBody2D.new()
		cell.add_child(static_body)
		var collision_shape = CollisionShape2D.new()
		var rectangle_shape = RectangleShape2D.new()
		rectangle_shape.size = fixed_cell_size
		collision_shape.shape = rectangle_shape
		static_body.add_child(collision_shape)

func create_number_tiles():
	tiles_label.add_theme_color_override("font_color", tiles_lines_color)
	
	var shuffled_animals = used_animals.duplicate()
	shuffled_animals.shuffle()
	var fixed_cell_size = get_responsive_cell_size()

	for i in range(shuffled_animals.size()):
		var animal_name = shuffled_animals[i]
		var all_color_images = ImageLoader.get_all_animal_color_images()
		var texture = all_color_images.get(animal_name, null)
		if texture == null:
			continue

		var sprite = Sprite2D.new()
		sprite.texture = texture
		sprite.scale = Vector2(fixed_cell_size.x / texture.get_width(), fixed_cell_size.y / texture.get_height())
		sprite.position = fixed_cell_size / 2

		var tile = Node2D.new()
		tile.name = animal_name
		tile.position = Vector2(i % board_size.x * fixed_cell_size.x, int(i / board_size.x) * fixed_cell_size.y)
		tile.set("animal_name", animal_name)
		tile.set("is_dragging", false)
		tile.set("correct_slot", null)
		tile.set_script(preload("res://scripts/emparejar_las_sombras/animal_tile.gd"))
		tile.set("tile_size", fixed_cell_size)
		
		tile.add_child(sprite)

		var border = Line2D.new()
		var margin = 5.0
		border.add_point(Vector2(margin, margin))
		border.add_point(Vector2(fixed_cell_size.x - margin, margin))
		border.add_point(Vector2(fixed_cell_size.x - margin, fixed_cell_size.y - margin))
		border.add_point(Vector2(margin, fixed_cell_size.y - margin))
		border.add_point(Vector2(margin, margin)) # cerrar la forma
		border.width = 4
		border.default_color = tiles_lines_color
		tile.add_child(border)

		var area = Area2D.new()
		tile.add_child(area)

		var collision_shape = CollisionShape2D.new()
		var rectangle_shape = RectangleShape2D.new()
		rectangle_shape.size = fixed_cell_size
		collision_shape.shape = rectangle_shape
		collision_shape.position = fixed_cell_size / 2
		area.add_child(collision_shape)

		tiles_container.add_child(tile)
		number_tiles.append(tile)

func check_win():
	var all_matched = true
	for child in game_board.get_node("GridContainer").get_children():
		if child is AnimalTile and child.correct_slot == null:
			all_matched = false
			break

	if all_matched:
		var popup = $WinPopup
		for p in GameData.partidas_activas:
			popup.get_node("VBoxContainer/victory_label").text = "✅ ¡Has ganado!"
			popup.get_node("VBoxContainer/fail_count_label").text = "❌ Fallos totales: %d" % p["fails"]
		popup.show()
		$Blocker.visible = true
		await GameData.end_game("", AppConstants.NAME_JUEGO_EMPAREJAR_LAS_SOMBRAS)
		
func _on_exit_button_pressed():
	SceneManager.go_back_to_previous_scene()
	
func set_tiles_interaction(enabled: bool) -> void:
	for tile in number_tiles:
		if tile is Node:
			tile.set_process_input(enabled)
