import 'dart:async';
import 'package:flutter/material.dart';
//import 'package:fluttertoast/fluttertoast.dart';

import 'package:mystrengthlog/db_provider.dart';
import 'package:mystrengthlog/models.dart';

class TodoBloc {

  List<Todo> allTodos = [];
  List<Todo> dateFilterdTodos = [];

  TodoBloc() {
    getAllTodos();
  }

  final _todoListController = StreamController<List<Todo>>.broadcast();
  final _todoController = StreamController<Todo>.broadcast();
//  final _todoDateController = StreamController<List<Todo>>.broadcast();
  final _todoDateController = StreamController<List<Todo>>();

  Stream<List<Todo>> get todoListStream => _todoListController.stream;
  Stream<Todo> get todoStream => _todoController.stream;
  Stream<List<Todo>> get todoDateStream => _todoDateController.stream;

  void getFilterdList(String name) {
    final todos = allTodos.where((element) => element.title == name).toList();
    _todoListController.sink.add(todos);
  }

  void dispose() {
    _todoListController.close();
    _todoController.close();
    _todoDateController.close();
  }

  void getAllTodos() async {
    allTodos = await DBProvider.db.getAllTodos();
    // descending order
    allTodos.sort((b, a) => a.date.compareTo(b.date));
    _todoListController.sink.add(allTodos);
  }

  void getTodoById(String id) async {
    _todoController.sink.add(await DBProvider.db.getTodoById(id));
  }

//  void getTodoByDate(DateTime date) async {
  void getTodoByDate(DateTime date) {

    dateFilterdTodos = allTodos.where((element) => element.date == date).toList();
    _todoDateController.sink.add(dateFilterdTodos);
//    _todoListController.sink.add(dateFilterdTodos);
  }

  void getTodoByName(String name) {

  }

  void addTodo(Todo todo) {
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