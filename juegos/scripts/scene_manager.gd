extends Node
var previous_scene: Node = null
var current_scene: Node = null

# Cambiar de escena: Agregar la nueva escena al árbol
func change_scene(new_scene: Node, actual_scene: Node):
	previous_scene = actual_scene.duplicate()  # Guardamos la escena anterior antes de liberar
	current_scene = new_scene      # Establecemos la nueva escena como la actual
	actual_scene.queue_free()      # Liberamos la escena actual
	get_tree().root.add_child(current_scene)  # Añadimos la nueva escena al árbol

# Volver a la escena anterior
func go_back_to_previous_scene():
	if previous_scene:
		current_scene.queue_free()  # Liberamos la escena actual
		get_tree().root.add_child(previous_scene)  # Volver a agregar la escena anterior
		current_scene = previous_scene  # Establecemos la escena actual como la anterior
		previous_scene = null  # Limpiamos la referencia a la escena anterior
	else:
		print("No hay una escena anterior para volver.")
