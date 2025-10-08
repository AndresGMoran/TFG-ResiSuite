extends Window

@onready var levels_menu = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/levels_menu
@onready var residents_menu = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/residents_menu
@onready var bg_color_picker = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/bg_color_picker
@onready var tiles_lines_color_picker = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/tiles_lines_color_picker
@onready var table_lines_color_picker = $MarginContainer/ScrollContainer/MarginContainer/VBoxContainer/table_lines_color_picker

var current_scene

var residents = []
var level = -1
var id_player = -1

func _ready():
	while residents.is_empty():
		await get_tree().process_frame
	
	_load_residents()
	levels_menu.get_popup().connect("id_pressed", self._on_level_selected)
	residents_menu.get_popup().connect("id_pressed", self._on_player_selected) 
		
func _load_residents() -> void:
	var popup = residents_menu.get_popup()
	popup.clear()

	for resident in residents:
		var full_name = "%s %s" % [resident.nombre, resident.apellido]
		var id = int(resident.id)
		popup.add_item(full_name, id)

### Metodo para cuando el usuario elija el nivel en el menu 
func _on_level_selected(id):
	match id:
		0:
			level = 0
			levels_menu.text = "Facil"
		1:
			level = 1
			levels_menu.text = "Medio"
		2:
			level = 2
			levels_menu.text = "Dificil"

### Metodo para cuando el usuario elija el jugador en el menu
func _on_player_selected(id):
	id_player = id
	for resident in residents:
		if resident.id == id:
			var full_name = "%s %s" % [resident.nombre, resident.apellido]
			residents_menu.text = full_name

### Metodo para cuando el usuario le de al boton de jugar
func _on_play_button_pressed() -> void:	
	var error_style = StyleBoxFlat.new()
	error_style.bg_color = Color(1, 0.8, 0.8)
	var normal_style = StyleBoxFlat.new()
	normal_style.bg_color = Color("#96a7af")
	var has_error := false

	# Validar jugadores
	if id_player == -1:
		residents_menu.add_theme_stylebox_override("normal", error_style)
		has_error = true
	else:
		residents_menu.add_theme_stylebox_override("normal", normal_style)
		
	if level == -1:
		levels_menu.add_theme_stylebox_override("normal", error_style)
		has_error = true
	else:
		levels_menu.add_theme_stylebox_override("normal", normal_style)

	if has_error:
		return
		
	
	var game_scene = preload("res://scenes/emparejar_las_sombras/GameEmparejarLasSombras.tscn").instantiate()
	
	game_scene.bg_color = bg_color_picker.color
	game_scene.tiles_lines_color = tiles_lines_color_picker.color
	game_scene.table_lines_color = table_lines_color_picker.color
	game_scene.player_selected = id_player
	game_scene.difficulty = level
	
	
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
	residents_menu.add_theme_stylebox_override("normal", normal_style)
	levels_menu.add_theme_stylebox_override("normal", normal_style)
	id_player = -1
	level = -1
	residents_menu.text = "Seleccionar jugador"
	levels_menu.text = "Seleccionar nivel"
	
	bg_color_picker.color = Color("ffffff")
	tiles_lines_color_picker.color = Color("f90000")
	table_lines_color_picker.color = Color("0000f8")
