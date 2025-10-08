extends Control

@onready var next_sound_button = $CenterContainer/VBoxContainer/NextSoundButton
@onready var repeat_sound_button = $CenterContainer/VBoxContainer/HBoxContainer/RepeatSoundButton
@onready var end_of_game_button = $CenterContainer/VBoxContainer/HBoxContainer/EndOfGameButton
@onready var animal_history_container = $CenterContainer/VBoxContainer/ColorRect/FlowContainerHistory
@onready var audio_player = $AudioStreamPlayer2D
@onready var winner_selector = $WinnerSelector
@onready var menu_players = $WinnerSelector/VBoxContainer/MenuPlayers

@onready var exit_menu_confirm_exit_button = $ExitButtonWindow/VBoxContainer/HBoxContainer/ConfirmExitButton
@onready var exit_menu_cancel_exit_button = $ExitButtonWindow/VBoxContainer/HBoxContainer/CancelExitButton

var animales = [
	{ "nombre": "Elfante", "sonido": preload("res://audios/bingo_auditivo/Elefant.mp3") },
	{ "nombre": "Caballo", "sonido": preload("res://audios/bingo_auditivo/caballo.mp3") },
	{ "nombre": "Asno", "sonido": preload("res://audios/bingo_auditivo/asno.mp3") },
	{ "nombre": "Cuervo", "sonido": preload("res://audios/bingo_auditivo/cuervo.mp3") },
	{ "nombre": "Gallo", "sonido": preload("res://audios/bingo_auditivo/gallo.mp3") },
	{ "nombre": "Gato", "sonido": preload("res://audios/bingo_auditivo/gato.mp3") },
	{ "nombre": "Gaviota", "sonido": preload("res://audios/bingo_auditivo/gaviota.mp3") },
	{ "nombre": "Oveja", "sonido": preload("res://audios/bingo_auditivo/oveja.mp3") },
	{ "nombre": "Perro", "sonido": preload("res://audios/bingo_auditivo/perro.mp3") },
	{ "nombre": "Vaca", "sonido": preload("res://audios/bingo_auditivo/vaca.mp3") },
	{ "nombre": "Lobo", "sonido": preload("res://audios/bingo_auditivo/wolf.mp3") }
	
]
var animal_emojis = {
	"Elfante": "🐘",
	"Caballo": "🐎",
	"Asno": "🐴",
	"Cuervo": "🪶",
	"Gallo": "🐓",
	"Gato": "🐱",
	"Gaviota": "🕊️",
	"Oveja": "🐑",
	"Perro": "🐶",
	"Vaca": "🐄",
	"Lobo": "🐺"
}

var animales_restantes = []
var sound_just_played

@export var jugadores_seleccionados = []

var ganador_index = -1
var ganador = null

func _ready():
	$Bloqueador.visible = true
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
	$Bloqueador.visible = false
	
	$MenuButton.get_popup().add_item("Salir", 0)
	$MenuButton.get_popup().connect("id_pressed", self._on_menu_item_selected)
	
	exit_menu_confirm_exit_button.pressed.connect(_on_exit_menu_button_pressed.bind(true))
	exit_menu_cancel_exit_button.pressed.connect(_on_exit_menu_button_pressed.bind(false))
	
	animales_restantes = animales.duplicate()
	randomize()
	
	for jugador in jugadores_seleccionados:
		GameData.start_game(jugador.id, 0, "")
	
	for i in jugadores_seleccionados.size():
		var full_name = jugadores_seleccionados[i].nombre + " " + jugadores_seleccionados[i].apellido
		menu_players.get_popup().add_item(full_name, i)
	menu_players.get_popup().connect("id_pressed", Callable(self, "_on_ganador_seleccionado"))
	
func _on_menu_item_selected(id):
	match id:
		0:
			$ExitButtonWindow.show()
			$Bloqueador.visible = true
			
func _on_exit_menu_button_pressed(exit : bool):
	if exit:
		GameData.clear_active_games()
		GameData.clear_completed_games()
		SceneManager.go_back_to_previous_scene()
	else:
		$ExitButtonWindow.hide()
		$Bloqueador.visible = false

func _on_next_sound_button_pressed():
	if animales_restantes.is_empty():
		winner_selector.show()
		$Bloqueador.visible = true
		return

	var index = randi() % animales_restantes.size()
	var animal = animales_restantes[index]
	sound_just_played = animal

	# Reproduce el sonido
	$AnimalAudio.stream = animal["sonido"]
	$AnimalAudio.play()
	
	# Crear y mostrar el emoji
	var emoji_label = Label.new()
	emoji_label.text = animal_emojis.get(animal["nombre"], "❓")
	emoji_label.add_theme_font_size_override("font_size", 62)

	animal_history_container.add_child(emoji_label)
	
	animales_restantes.erase(animal)
	
func _on_repeat_sound_button_pressed():
	$AnimalAudio.stream = sound_just_played["sonido"]
	$AnimalAudio.play()
	
func _on_end_of_game_button_pressed():
	winner_selector.show()
	$Bloqueador.visible = true
	
func _on_ganador_seleccionado(index: int):
	if ganador_index != -1:
		GameData.remove_victory_by_resident(jugadores_seleccionados[ganador_index].id)

	ganador_index = index
	ganador = jugadores_seleccionados[index]
	GameData.add_victory_by_resident(ganador.id)

	var full_name = "%s %s" % [ganador.nombre, ganador.apellido]
	menu_players.text = full_name

	
func _on_winner_button_pressed():
	GameData.end_game("", AppConstants.NAME_JUEGO_BINGO_AUDITIVO)
	SceneManager.go_back_to_previous_scene()
	
func _on_close_button_pressed():
	winner_selector.hide()
	if $Bloqueador.visible:
		$Bloqueador.visible = false
