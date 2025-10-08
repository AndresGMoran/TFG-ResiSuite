extends Node

signal residents_received(residents)
signal games_received(games)
signal login_successful()
signal login_failed(error_msg)

var all_residents = []
var all_games = []

var is_logged_in: bool = false
var auth_token: String = ""
var token_expiration: int = 0
var residencia_id: int = -1
var user_id = -1

var partidas_activas: Array = []
var partidas_completadas: Array = []

# -------------------------- LOGIN --------------------------

func login(email: String, password: String):
	var http_request = HTTPRequest.new()
	get_tree().root.add_child(http_request)
	http_request.connect("request_completed", Callable(self, "_on_login_response").bind(http_request))

	var data = {
		"email": email,
		"password": password
	}
	var json_body = JSON.stringify(data)
	var headers = ["Content-Type: application/json"]
	var error = http_request.request(AppConstants.ENDPOINT_LOG_IN, headers, HTTPClient.METHOD_POST, json_body)

	if error != OK:
		print("[LOGIN] ❌ Error al iniciar la solicitud de login:", error)

func _on_login_response(result, response_code, headers, body, request_node):
	print("[LOGIN] 📡 Código de respuesta:", response_code)
	if response_code == 200:
		var parsed = JSON.parse_string(body.get_string_from_utf8())
		if parsed and parsed.has("token"):
			auth_token = parsed["token"]
			token_expiration = parsed.get("expiresIn", 0)
			user_id = parsed.get("idUser", -1)
			residencia_id = parsed.get("idResidencia", -1)
			is_logged_in = true
			print("[LOGIN] ✅ Login exitoso. Usuario ID: %d, Residencia ID: %d" % [user_id, residencia_id])
			emit_signal("login_successful")
		else:
			print("[LOGIN] ⚠️ Respuesta sin token recibido.")
			emit_signal("login_failed", "Respuesta sin token.")
	else:
		print("[LOGIN] ❌ Fallo en login. Código de respuesta:", response_code)
		emit_signal("login_failed", "Login fallido. Código: %d" % response_code)

	request_node.queue_free()

# -------------------------- INICIO Y FIN DE PARTIDA --------------------------

func start_game(resident, level, level_name):
	var partida = {
		"idUser": user_id,
		"idResident": resident,
		"start_time": Time.get_unix_time_from_system(),
		"level_name": level_name,
		"level": level,
		"fails": 0
	}
	partidas_activas.append(partida)
	print("[JUEGO] ▶️ Partida iniciada para residente ID %d en nivel %d" % [resident, level])

func end_game(level_name : String, game_name : String):
	var now = Time.get_unix_time_from_system()

	var games = await get_games_loaded()
	var game_id = 0
	for game in games:
		if game.nombre.to_lower() == game_name.to_lower():
			game_id = game.id
			break
			
	if game_id == 0:
		print("[PARTIDA] ⚠️ No se encontró el juego en la base de datos.")
		return

	var partidas_a_borrar := []
	
# Lógica específica para Gimnasio
	if game_name.to_lower() == AppConstants.NAME_JUEGO_GIMNASIO.to_lower():
		for partida in partidas_activas:
			if partida.level_name == level_name:
				var tiempo_total = now - partida["start_time"]
				var tiempo_previo := 0

				# SOLO sumar duración de partidas completadas del mismo residente
				for p in partidas_completadas:
					if p["idResident"] == partida["idResident"]:
						tiempo_previo += p["duracion"]

				var duracion_real = max(tiempo_total - tiempo_previo, 0)

				var data = {
					"idResidente": partida["idResident"],
					"idUsuario": partida["idUser"],
					"num": 0,
					"duracion": duracion_real,
					"dificultad": partida["level"]
				}

				print("[JUEGO] 🛑 Partida", partida["level_name"], "Residente", data["idResidente"], "Duración:", duracion_real ,"segundos.")
				send_game_data(data, game_id)

				partida["duracion"] = duracion_real
				partidas_completadas.append(partida)

		# Borrar SOLO las partidas activas del ejercicio actual
		partidas_activas = partidas_activas.filter(func(p): return p.level_name != level_name)

		if partidas_activas.is_empty():
			clear_completed_games()

	# Lógica específica para Bingo auditivo y Flecha y reacciona
	elif game_name.to_lower() == AppConstants.NAME_JUEGO_BINGO_AUDITIVO.to_lower() or game_name.to_lower() == AppConstants.NAME_JUEGO_FLECHA_Y_REACCIONA.to_lower():
		for partida in partidas_activas:
			var duracion = now - partida["start_time"]

			var data = {
				"idResidente": partida["idResident"],
				"idUsuario": partida["idUser"],  
				"num": partida["fails"],
				"duracion": duracion,
				"dificultad": partida["level"]
			}

			print("[PARTIDA] 📤 Enviando datos - Residente ID: %d, Duración: %d, Nivel: %d" % [data["idResidente"], duracion, data["dificultad"]])
			send_game_data(data, game_id)

		clear_active_games()
		if partidas_activas.is_empty() and !partidas_completadas.is_empty():
			clear_completed_games()
			
	# Lógica especifica para Seguir la linea y Emparejar las sombras
	else:
		for partida in partidas_activas:
			if partida.level_name.to_lower() == level_name.to_lower():
				var tiempo_total = now - partida["start_time"]
				var tiempo_previo := 0

				for p in partidas_completadas:
					tiempo_previo += p["duracion"]

				var duracion_real = max(tiempo_total - tiempo_previo, 0)

				var data = {
					"idResidente": partida["idResident"],
					"idUsuario": partida["idUser"],
					"num": partida["fails"],
					"duracion": duracion_real,
					"dificultad": partida["level"]
				}

				print("[JUEGO] 🛑 Partida", partida["level_name"], "finalizada. Duración:", duracion_real ,"segundos. Fallos:", partida["fails"])
				send_game_data(data, game_id)

				partida["duracion"] = duracion_real
				partidas_completadas.append(partida)
				partidas_activas.erase(partida)

	
func add_fail(level_name: String):
	for partida in partidas_activas:
		if partida.level_name == level_name:
			partida["fails"] += 1
			print("[JUEGO] ❌ Fallo añadido a ", level_name ,". Total fallos: ", partida["fails"])
			break
			
func add_victory_by_resident(resident):
	for partida in partidas_activas:
		if partida.idResident == resident:
			partida["fails"] = 1
			print("[JUEGO] Victoria añadido a ", resident)
			break

func remove_victory_by_resident(resident):
	for partida in partidas_activas:
		if partida.idResident == resident:
			partida["fails"] = 0
			print("[JUEGO] Victoria quitado a ", resident)
			break
			
# Borra todas las partidas activas
func clear_active_games():
	partidas_activas.clear()
	print("[PARTIDA] 🗑️ Partidas activas limpiadas.")
	
func clear_completed_games():
	partidas_completadas.clear()
	print("[PARTIDA] 🗑️ Partidas completadas limpiadas.")

# Borra una partida activa específica por nombre
func remove_active_game(name: String):
	for partida in partidas_activas:
		if name == partida["level_name"]:
			partidas_activas.erase(partida)
			print("Partida ", name, " eliminada de partidas activas")

# -------------------------- GET RESIDENTS --------------------------

func get_residents():
	var http_request = HTTPRequest.new()
	get_tree().root.add_child(http_request)
	http_request.connect("request_completed", Callable(self, "_on_request_get_residents").bind(http_request))

	var headers = [
		"User-Agent: Godot",
		"Authorization: Bearer %s" % auth_token
	]
	var error = http_request.request(AppConstants.ENDPOINT_GET_ALL_RESIDENTS, headers)

	if error != OK:
		print("[RESIDENTES] ❌ Error al hacer GET de residentes:", error)

func _on_request_get_residents(result, response_code, headers, body, request_node):
	print("[RESIDENTES] 📡 Código de respuesta:", response_code)
	if response_code == 200:
		var parsed = JSON.parse_string(body.get_string_from_utf8())
		if parsed != null:
			all_residents = parsed
			print("[RESIDENTES] ✅ Residentes cargados:", all_residents.size())
			emit_signal("residents_received", all_residents)
		else:
			print("[RESIDENTES] ❌ Error al parsear JSON de residentes.")
	else:
		print("[RESIDENTES] ❌ Error al obtener residentes. Código:", response_code)

	request_node.queue_free()

func get_residents_loaded() -> Array:
	get_residents()
	var result = await self.residents_received
	return result

# -------------------------- GET GAMES --------------------------

func get_games():
	var http_request = HTTPRequest.new()
	get_tree().root.add_child(http_request)
	http_request.connect("request_completed", Callable(self, "_on_request_get_games").bind(http_request))

	var headers = [
		"User-Agent: Godot",
		"Authorization: Bearer %s" % auth_token
	]
	var error = http_request.request(AppConstants.ENDPOINT_GET_ALL_GAMES, headers)

	if error != OK:
		print("[JUEGOS] ❌ Error al hacer GET de juegos:", error)

func _on_request_get_games(result, response_code, headers, body, request_node):
	print("[JUEGOS] 📡 Código de respuesta:", response_code)
	if response_code == 200:
		var parsed = JSON.parse_string(body.get_string_from_utf8())
		if parsed != null:
			all_games = parsed
			print("[JUEGOS] ✅ Juegos cargados:", all_games.size())
			emit_signal("games_received", all_games)
		else:
			print("[JUEGOS] ❌ Error al parsear JSON de juegos.")
	else:
		print("[JUEGOS] ❌ Error al obtener juegos. Código:", response_code)

	request_node.queue_free()

func get_games_loaded() -> Array:
	get_games()
	var result = await self.games_received
	return result

# -------------------------- POST PARTIDA --------------------------

func send_game_data(data: Dictionary, game_id):
	print("[PARTIDA] ▶️ Body: ", data, " . Residencia: ", residencia_id, " . Juego: ", game_id)
	var http_request = HTTPRequest.new()
	get_tree().root.add_child(http_request)

	var json_body = JSON.stringify(data)
	var headers = [
		"Content-Type: application/json",
		"Authorization: Bearer %s" % auth_token
	]
	var url = AppConstants.ENDPOINT_POST_GAME_STATS % [game_id]

	http_request.connect("request_completed", Callable(self, "_on_game_data_sent").bind(http_request))
	var error = http_request.request(url, headers, HTTPClient.METHOD_POST, json_body)

	if error != OK:
		print("[PARTIDA] ❌ Error al enviar los datos de la partida:", error)

func _on_game_data_sent(result, response_code, headers, body, http_request):
	if response_code == 200 or response_code == 201:
		print("[PARTIDA] ✅ Datos de partida registrados correctamente.")
	else:
		print("[PARTIDA] ❌ Error al registrar la partida. Código:", response_code)
