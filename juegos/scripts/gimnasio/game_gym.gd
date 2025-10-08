extends Control

@onready var video_player = $VideoStreamPlayer
@onready var menu = $HBoxContainer/MenuButton
@onready var audio_player = $AudioStreamPlayer2D

@onready var exit_menu_confirm_exit_button = $ExitButtonWindow/VBoxContainer/HBoxContainer/ConfirmExitButton
@onready var exit_menu_dont_exit_button = $ExitButtonWindow/VBoxContainer/HBoxContainer/DontExitButton

var videos: Array[Dictionary] = []
var excercises_order = []
var ordered_videos = []
var current_index: int = 0
var players_selected = []

func _ready():
	videos = [
		{"name" : "Marcha", "video" : preload("res://videos/gimnasio/1_marcha.ogv"), "level": 0},
		{"name" : "Brazos al lado", "video" : preload("res://videos/gimnasio/2_alcance_vertical.ogv"), "level": 0},
		{"name" : "Brazos arriba sentado", "video" : preload("res://videos/gimnasio/6_levantar_brazos_sentado.ogv"), "level": 0},
		{"name" : "Sentar y levantar", "video" : preload("res://videos/gimnasio/7_levantarse_sentarse.ogv"), "level": 1},
		{"name" : "Nadar", "video" : preload("res://videos/gimnasio/10_nadar_en_el_aire.ogv"), "level": 1},
		{"name" : "Movimientos laterales", "video" : preload("res://videos/gimnasio/8_movimientos_laterales.ogv"), "level": 1},
		{"name" : "Moviminetos laterales + puntillas", "video" : preload("res://videos/gimnasio/9_movimientos_laterales_con_puntillas.ogv"), "level": 1},
		{"name" : "Pesa arriba sentado", "video" : preload("res://videos/gimnasio/3_pesa-arriba_sentado.ogv"), "level": 2},
		{"name" : "Pesa atras sentado", "video" : preload("res://videos/gimnasio/4_pesa_atras_sentado.ogv"), "level": 2},
		{"name" : "Pesas doble sentado", "video" : preload("res://videos/gimnasio/5_pesas_dobles_sentado.ogv"), "level": 2}
	]
	
	await get_tree().process_frame
	
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

	$Blocker.visible = false
	$Charecter.visible = false
	$TextBallon.visible = false
	
	for excercise_name in excercises_order:
		for video in videos:
			if video["name"] == excercise_name:
				ordered_videos.append(video)
				break
				
	for player in players_selected:
		for video in ordered_videos:
			GameData.start_game(player.id, video.level, video.name)
			
	
	menu.get_popup().add_item("Reproducir sonido", 0)
	menu.get_popup().add_item("Salir", 1)
	menu.get_popup().connect("id_pressed", self._on_menu_item_selected)
	
	exit_menu_confirm_exit_button.pressed.connect(_on_exit_menu_button_pressed.bind(true))
	exit_menu_dont_exit_button.pressed.connect(_on_exit_menu_button_pressed.bind(false))

	video_player.stream = ordered_videos[current_index].video
	video_player.play()
	
func _on_menu_item_selected(id):
	match id:
		0:
			audio_player.play()
		1:
			$ExitButtonWindow.show()
			$Blocker.visible = true


func _on_next_video_button_pressed():
	current_index += 1
	if current_index < ordered_videos.size():
		for player in players_selected:
			GameData.end_game(ordered_videos.get(current_index - 1).name, AppConstants.NAME_JUEGO_GIMNASIO)

		video_player.stream = ordered_videos[current_index].video
		video_player.play()
	else:
		$EndOfVideosWindow.show()
		$Blocker.visible = true
		$Blocker.get
		video_player.stop()

func _on_exit_menu_button_pressed(exit : bool):
	if exit:
		GameData.clear_active_games()
		GameData.clear_completed_games()
		SceneManager.go_back_to_previous_scene()
	else:
		$ExitButtonWindow.hide()
		$Blocker.visible = false
		
func _on_end_of_videos_button_pressed():
	for player in players_selected:
		GameData.end_game(ordered_videos.get(current_index - 1).name, AppConstants.NAME_JUEGO_GIMNASIO)
	SceneManager.go_back_to_previous_scene()
