class_name AnimalTile
extends Node2D

var dragging = false
var drag_offset = Vector2()
var original_position = Vector2()

var tile_size: Vector2 = Vector2.ZERO

func _ready():
	original_position = global_position

func _input(event):
	var is_touch = event is InputEventScreenTouch
	var is_mouse_button = event is InputEventMouseButton
	var is_mouse_motion = event is InputEventMouseMotion
	var is_screen_drag = event is InputEventScreenDrag

	# Iniciar arrastre
	if (is_touch and event.pressed) or (is_mouse_button and event.button_index == MOUSE_BUTTON_LEFT and event.pressed):
		if not dragging:
			var input_pos = event.position
			if get_tile_rect().has_point(input_pos):
				dragging = true
				drag_offset = global_position - input_pos
				original_position = global_position
				z_index = 10

	# Soltar
	if (is_touch and not event.pressed and dragging) or (is_mouse_button and event.button_index == MOUSE_BUTTON_LEFT and not event.pressed and dragging):
		if dragging:
			dragging = false
			z_index = 0
			check_drop_on_board()

	# Arrastrar
	if (is_screen_drag and dragging) or (is_mouse_motion and dragging and Input.is_mouse_button_pressed(MOUSE_BUTTON_LEFT)):
		global_position = event.position + drag_offset
			
func get_tile_rect() -> Rect2:
	var sprite = get_node_or_null("Sprite2D")
	if sprite:
		var size = sprite.texture.get_size() * sprite.scale
		return Rect2(global_position - size / 2, size)
	return Rect2(global_position, tile_size) 

func check_drop_on_board():
	# Ruta actualizada según jerarquía corregida
	if not get_tree().get_root().has_node("Node2D/VBoxContainer/MarginContainer/HBoxContainer/VBoxContainer2/GameBoardContainer/GameBoard/GridContainer"):
		return

	var game_board = get_tree().get_root().get_node("Node2D/VBoxContainer/MarginContainer/HBoxContainer/VBoxContainer2/GameBoardContainer/GameBoard/GridContainer")

	var tile_rect = get_tile_rect()
	var placed_on_board = false

	for i in range(game_board.get_child_count()):
		var slot = game_board.get_child(i)
		if slot is Node:
			var slot_rect = Rect2(slot.global_position, tile_size) 
			if slot_rect.has_point(tile_rect.get_center()) and self.name == slot.name and not slot.get("is_occupied"):
				global_position = slot.global_position
				var fixed_cell_size = tile_size 
				if slot.has_node("Sprite2D"):
					var color_images = ImageLoader.get_all_animal_color_images()
					var dibujo_texture = color_images.get(self.name, null)

					if dibujo_texture:
						var sprite = slot.get_node("Sprite2D")
						var slot_size = sprite.texture.get_size() * sprite.scale  

						sprite.texture = dibujo_texture
						var new_size = dibujo_texture.get_size()
						sprite.scale = slot_size / new_size
						sprite.position = slot_size / 2



					else:
						push_warning("No se encontró textura color para: " + self.name)

				set_process_input(false)
				dragging = false
				slot.set("is_occupied", true)
				queue_free() # Destruimos el tile porque ya está colocado
				
				var tiles_container = get_tree().get_root().get_node("Node2D/VBoxContainer/MarginContainer/HBoxContainer/VBoxContainer/TilesContainer")
				if tiles_container.get_child_count() <= 1:
					get_tree().get_root().get_node("Node2D").check_win()
				placed_on_board = true
				break

	if not placed_on_board:
		global_position = original_position
		GameData.add_fail("")
