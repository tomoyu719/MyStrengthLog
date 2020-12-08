import 'package:flutter/foundation.dart';
import 'package:uuid/uuid.dart';
import 'package:flutter/material.dart';

class Todo {
//  String id;
//  DateTime date;
//  String title;
//  String description;
  final String id;
  final DateTime date;
  final String title;
  final String description;

  Todo(
      {this.id, this.date, this.title, this.description}
  );

  //    id = Uuid().v4();
//    date = DateTime.now();
//    title = "";
//    description = "";


  factory Todo.fromMap(Map<String, dynamic> json) => new Todo(
      id: json["id"],
      date: DateTime.parse(json["date"]).toLocal(),
      title: json["title"],
      description: json["description"]
  );

  Map<String, dynamic> toMap() => {
    "id": id,
    "date": date.toUtc().toIso8601String(),
    "title": title,
    "description": description
  };
}