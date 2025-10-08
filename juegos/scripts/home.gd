extends Control

@onready var login_popup = $LogIn
@onready var email_input = $LogIn/VBoxContainer/EmailInput
@onready var password_input = $LogIn/VBoxContainer/PasswordInput
@onready var login_button = $LogIn/VBoxContainer/LogInButton

@onready var configuration_f_y_r = $ConfigurationFYR
@onready var configuration_bingo = $ConfigurationBingo
@onready var configuration_gimnasio = $ConfigurationGimnasio
@onready var configuration_seguir_la_linea = $ConfigurationSeguirLaLinea
@onready var configuration_emparejar_las_sombras = $ConfigurationoEmparejarLasSombras

@onready var button_emarejar_las_sombras = $MarginContainer/VBoxContainer/FlowContainer/Button
@onready var button_seguir_la_linea = $MarginContainer/VBoxContainer/FlowContainer/Button2
@onready var button_gimnasio = $MarginContainer/VBoxContainer/FlowContainer/Button3
@onready var button_bingo_auditivo = $MarginContainer/VBoxContainer/FlowContainer/Button4
@onready var button_flecha_y_reacciona = $MarginContainer/VBoxContainer/FlowContainer/Button5


func _ready():
	RenderingServer.set_default_clear_color("#CCCCCC")
	
	await get_tree().process_frame  # Esperar a que todo esté listo
	
	if not GameData.is_logged_in:
		login_popup.show()
		$Blocker.visible = true
			
	button_emarejar_las_sombras.pressed.connect(_on_game_selected.bind(button_emarejar_las_sombras))
	button_seguir_la_linea.pressed.connect(_on_game_selected.bind(button_seguir_la_linea))
	button_gimnasio.pressed.connect(_on_game_selected.bind(button_gimnasio))
	button_bingo_auditivo.pressed.connect(_on_game_selected.bind(button_bingo_auditivo))
	button_flecha_y_reacciona.pressed.connect(_on_game_selected.bind(button_flecha_y_reacciona))
	
### Metodo para cuando se pulse el boton de iniciar sesion
func _on_log_in_button_pressed():
	var username = email_input.text.strip_edges()
	var password = password_input.text.strip_edges()

	GameData.login(username, password)

	GameData.connect("login_successful", Callable(self, "_on_login_successful"), CONNECT_ONE_SHOT)
	GameData.connect("login_failed", Callable(self, "_on_login_failed"), CONNECT_ONE_SHOT)

func _on_login_successful():
	login_popup.hide()
	$Blocker.visible = false

func _on_login_failed(error_msg: String):
	email_input.clear()
	password_input.clear()

	var stylebox_email = email_input.get_theme_stylebox("normal") as StyleBoxFlat
	var stylebox_password = password_input.get_theme_stylebox("normal") as StyleBoxFlat
	if stylebox_email and stylebox_password:
		stylebox_email.bg_color = Color(1, 0.8, 0.8)
		stylebox_password.bg_color = Color(1, 0.8, 0.8)
	
func _on_game_selected(button : Button):
	if (button == button_emarejar_las_sombras):
		var residents = await GameData.get_residents_loaded()
		configuration_emparejar_las_sombras.default_config()
		configuration_emparejar_las_sombras.current_scene = self
		configuration_emparejar_las_sombras.residents = residents
		configuration_emparejar_las_sombras.show()
		$Blocker.visible = true
	if (button == button_seguir_la_linea):
		var residents = await GameData.get_residents_loaded()
		configuration_seguir_la_linea.default_config()
		configuration_seguir_la_linea.current_scene = self
		configuration_seguir_la_linea.residents = residents
		configuration_seguir_la_linea.show()
		$Blocker.visible = true
	if (button == button_gimnasio):
		var residents = await GameData.get_residents_loaded()
		configuration_gimnasio.default_config()
		configuration_gimnasio.current_scene = self
		configuration_gimnasio.residents = residents
		configuration_gimnasio.show()
		$Blocker.visible = true
	if (button == button_bingo_auditivo):
		var residents = await GameData.get_residents_loaded()
		configuration_bingo.default_config()
		configuration_bingo.current_scene = self
		configuration_bingo.residents = residents
		configuration_bingo.show()
		$Blocker.visible = true
	if (button == button_flecha_y_reacciona):
		var residents = await GameData.get_residents_loaded()
		configuration_f_y_r.default_config()
		configuration_f_y_r.current_scene = self
		configuration_f_y_r.residents = residents
		configuration_f_y_r.show()
		$Blocker.visible = true
