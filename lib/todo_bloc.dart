//import 'package:mystrengthlog/db_provider.dart';
import 'dart:async';

import 'package:mystrengthlog/db_provider.dart';
import 'package:mystrengthlog/repository.dart';
import 'package:mystrengthlog/models.dart';


class TodoBloc {

  TodoBloc() {
    getAllTodos();
  }

  final _todoListController = StreamController<List<Todo>>();
  final _todoController = StreamController<Todo>();
  Stream<List<Todo>> get todoListStream => _todoListController.stream;
  Stream<Todo> get todoStream => _todoController.stream;

  dispose() {
    _todoListController.close();
    _todoController.close();
  }


  Future<List<Todo>> getAllTodos() async {
    _todoListController.sink.add(await DBProvider.db.getAllTodos());
  }

//  Future<Todo> getTodoById(String id) async {
  getTodoById(String id) async {
    _todoController.sink.add(await DBProvider.db.getTodoById(id));
  }

  addTodo(Todo todo) {
//  addTodo() {
//    Todo todo = Todo(
//        id: '2',
//          date: DateTime.now(),
//        title: 'b',
//        description: 'aho2'
//  );
    DBProvider.db.addTodo(todo);
    getAllTodos();
  }

//  updateTodo(Todo todo) {
//    DBProvider.db.updateTodo(todo);
//    getAllTodos();
//  }

  deleteTodo(String id) {
    DBProvider.db.deleteTodo(id);
    getAllTodos();
  }
}