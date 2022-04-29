import {Injectable} from '@angular/core';
import {catchError, map, Observable, of} from "rxjs";
import {Droplet} from "./droplet";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class DropletService {
  private dropletsUrl = 'http://127.0.0.1:8080/droplets';  // URL to web api

  constructor(private http: HttpClient) {
  }

  /** Get names of droplets, transform to partial Droplet objects */
  getList(): Observable<Droplet[]> {
    return this.http.get<string[]>(this.dropletsUrl)
      .pipe(
        map(names => names.map(function (name: string): Droplet {
          return {name: name};
        })),
        catchError(this.handleError<Droplet[]>('getList', []))
      );
  }

  /** Get detail info about specified droplet */
  getOne(name: string): Observable<Droplet> {
    return this.http.get<Droplet>(this.dropletsUrl + "/" + name)
      .pipe(
        catchError(this.handleError<Droplet>('getOne', {name: "-error-"}))
      );
  }

  /** Execute droplet with specified args, return droplet answer */
  execute(droplet: Droplet, args: string[]): Observable<string> {
    const form = new FormData();
    for (let i = 0; i < args.length; i++) {
      form.append("arguments", args[i]);
    }
    return this.http.post<string>(this.dropletsUrl + "/" + droplet.name, form)
      .pipe(
        catchError(this.handleError<string>('execute', "-error-"))
      );
  }

  /** Upload new droplet */
  upload(name: string, jar: File): Observable<Droplet> {
    const form = new FormData();
    form.append("file", jar);
    return this.http.put<Droplet>(this.dropletsUrl + "/" + name, form)
      .pipe(
        catchError(this.handleError<Droplet>('upload', {name: "-error-"}))
      );
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   *
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
