import 'dart:io';

import 'models.dart';

import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

class DBProvider {
  DBProvider._();
  static final DBProvider db = DBProvider._();

  static Database _database;
  static final _tableName = "Todo";

  Future<Database> get database async {
    if (_database != null)
      return _database;

    _database = await initDB();
    return _database;
  }

  Future<Database> initDB() async {
    Directory documentsDirectory = await getApplicationDocumentsDirectory();
    String path = join(documentsDirectory.path, "TodoDB.db");

    return await openDatabase(path, version: 1, onCreate: _createTable);
  }

  Future<void> _createTable(Database db, int version) async {

    return await db.execute(
        "CREATE TABLE $_tableName ("
            "id TEXT PRIMARY KEY,"
            "title TEXT,"
            "date TEXT,"
            "description TEXT"
            ")"
    );
  }

  Future<List<Todo>> getAllTodos() async {
    final db = await database;
    var res = await db.query(_tableName);
    List<Todo> list =
    res.isNotEmpty ? res.map((c) => Todo.fromMap(c)).toList() : [];
    return list;
  }

  Future<Todo> getTodoById(String id) async {
    final db = await database;
    var res = await db.query(_tableName, where: "id = ?", whereArgs: [id]);
    return res.isNotEmpty ? Todo.fromMap(res.first) : Null;
  }

  addTodo(Todo todo) async {
    final db = await database;
    var res = await db.insert(_tableName, todo.toMap());
    return res;
  }

//  updateTodo(Todo todo) async {
//
//  }


//  getTodoByDate(String id) async {
//
//  }
//  getTodoByTitle(String id) async {
//
//  }
//
  deleteTodo(String id) async {
    final db = await database;
    db.delete(_tableName, where: "id = ?", whereArgs: [id]);
  }

}

